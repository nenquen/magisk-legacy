#include <libgen.h>
#include <dlfcn.h>
#include <sys/mount.h>
#include <sys/sendfile.h>
#include <sys/prctl.h>
#include <android/log.h>
#include <atomic>

#include <utils.hpp>

#include "inject.hpp"

using namespace std;

static void *self_handle = nullptr;
static atomic<int> active_threads = -1;

#define alog(prio) [](auto fmt, auto ap){ \
return __android_log_vprint(ANDROID_LOG_##prio, "Magisk", fmt, ap); }
static void inject_logging() {
    log_cb.d = alog(DEBUG);
    log_cb.i = alog(INFO);
    log_cb.w = alog(WARN);
    log_cb.e = alog(ERROR);
    log_cb.ex = nop_ex;
}

__attribute__((destructor))
static void inject_cleanup() {
    if (active_threads < 0)
        return;

    // Setup 1ms
    timespec ts = { .tv_sec = 0, .tv_nsec = 1000000L };

    // Check flag in busy loop
    while (active_threads)
        nanosleep(&ts, nullptr);

    // Wait another 1ms to make sure all threads left our code
    nanosleep(&ts, nullptr);
}

void self_unload() {
    LOGD("hook: Request to self unload\n");
    // If unhook failed, do not unload or else it will cause SIGSEGV
    if (!unhook_functions())
        return;
    new_daemon_thread(reinterpret_cast<void *(*)(void *)>(&dlclose), self_handle);
    active_threads--;
}

static void *unload_first_stage(void *) {
    // Setup 1ms
    timespec ts = { .tv_sec = 0, .tv_nsec = 1000000L };

    while (getenv(INJECT_ENV_1))
        nanosleep(&ts, nullptr);

    // Wait another 1ms to make sure all threads left our code
    nanosleep(&ts, nullptr);

    unmap_all(get_inject_lib_1());
    active_threads--;
    return nullptr;
}

const char *detect_magisk_tmp() {
    static char tmp_path[128] = {};
    if (tmp_path[0])
        return tmp_path;

    // Check env first (set by app_process_main)
    const char *env = getenv(INJECT_DIR_ENV);
    if (env) {
        strlcpy(tmp_path, env, sizeof(tmp_path));
        return tmp_path;
    }

    // Try /sbin (pre-Android 11 or some devices)
    if (access("/sbin", F_OK) == 0) {
        strlcpy(tmp_path, "/sbin", sizeof(tmp_path));
        return tmp_path;
    }

    // Scan /proc/self/mounts for magisk tmpfs under /dev/
    auto fp = open_file("/proc/self/mounts", "re");
    if (fp) {
        char buf[4096];
        while (fgets(buf, sizeof(buf), fp.get())) {
            char dev[256] = {}, mnt[256] = {}, type[64] = {};
            sscanf(buf, "%255s %255s %63s", dev, mnt, type);
            if (strcmp(type, "tmpfs") == 0 && str_starts(mnt, "/dev/") &&
                strlen(mnt) > 5) {
                // Found a tmpfs under /dev - likely MAGISKTMP
                strlcpy(tmp_path, mnt, sizeof(tmp_path));
                return tmp_path;
            }
        }
    }

    // Fallback to /dev/tmp (legacy)
    strlcpy(tmp_path, "/dev/tmp", sizeof(tmp_path));
    return tmp_path;
}

const char *get_inject_lib_1() {
    static char path[256] = {};
    if (path[0])
        return path;
    snprintf(path, sizeof(path), "%s/magisk.1.so", detect_magisk_tmp());
    return path;
}

const char *get_inject_lib_2() {
    static char path[256] = {};
    if (path[0])
        return path;
    snprintf(path, sizeof(path), "%s/magisk.2.so", detect_magisk_tmp());
    return path;
}

// Make sure /proc/self/environ does not reveal our secrets
// Copy all env to a contiguous memory and set the memory region as MM_ENV
static void sanitize_environ() {
    static string env;

    for (int i = 0; environ[i]; ++i) {
        if (str_starts(environ[i], INJECT_ENV_1 "="))
            continue;
        env += environ[i];
        env += '\0';
    }

    for (int i = 0; i < 2; ++i) {
        bool success = true;
        success &= (0 <= prctl(PR_SET_MM, PR_SET_MM_ENV_START, env.data(), 0, 0));
        success &= (0 <= prctl(PR_SET_MM, PR_SET_MM_ENV_END, env.data() + env.size(), 0, 0));
        if (success)
            break;
    }
}

__attribute__((constructor))
static void inject_init() {
    inject_logging();
    const char *lib1 = get_inject_lib_1();
    const char *lib2 = get_inject_lib_2();

    if (getenv(INJECT_ENV_2)) {
        LOGD("zygote: inject 2nd stage\n");
        active_threads = 1;
        unsetenv(INJECT_ENV_2);

        // Get our own handle
        self_handle = dlopen(lib2, RTLD_LAZY);
        dlclose(self_handle);

        hook_functions();

        // Some cleanup
        sanitize_environ();
        active_threads++;
        new_daemon_thread(&unload_first_stage);
    } else if (char *env = getenv(INJECT_ENV_1)) {
        LOGD("zygote: inject 1st stage\n");

        if (env[0] == '1')
            unsetenv("LD_PRELOAD");
        else
            setenv("LD_PRELOAD", env, 1);  // Restore original LD_PRELOAD

        // Setup second stage
        setenv(INJECT_ENV_2, "1", 1);
        cp_afc(lib1, lib2);
        dlopen(lib2, RTLD_LAZY);

        unsetenv(INJECT_ENV_1);
    }
}

int app_process_main(int argc, char *argv[]) {
    inject_logging();
    char buf[4096];
    if (realpath("/proc/self/exe", buf) == nullptr)
        return 1;

    // Detect MAGISKTMP and pass to injected library via env
    setenv(INJECT_DIR_ENV, detect_magisk_tmp(), 1);

    const char *lib1 = get_inject_lib_1();
    const char *lib2 = get_inject_lib_2();

    int in = xopen(buf, O_RDONLY);
    int out = xopen(lib1, O_CREAT | O_WRONLY | O_TRUNC, 0777);
    sendfile(out, in, nullptr, INT_MAX);
    close(in);
    close(out);

    char preload_env[512];
    if (char *ld = getenv("LD_PRELOAD")) {
        snprintf(preload_env, sizeof(preload_env), "%s:%s", ld, lib1);
        setenv("LD_PRELOAD", preload_env, 1);
        setenv(INJECT_ENV_1, ld, 1);  // Backup original LD_PRELOAD
    } else {
        setenv("LD_PRELOAD", lib1, 1);
        setenv(INJECT_ENV_1, "1", 1);
    }

    // Execute real app_process
    xumount2(buf, MNT_DETACH);
    execve(buf, argv, environ);
    return 1;
}
