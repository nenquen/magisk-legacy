#pragma once

#include <stdint.h>
#include <jni.h>

#define INJECT_DIR_ENV  "MAGISK_INJ_DIR"
#define INJECT_ENV_1    "MAGISK_INJ_1"
#define INJECT_ENV_2    "MAGISK_INJ_2"

// Resolve injected library paths using MAGISKTMP
const char *get_inject_lib_1();
const char *get_inject_lib_2();

// Detect MAGISKTMP at runtime (try env, then scan /proc/self/mounts)
const char *detect_magisk_tmp();

// Unmap all pages matching the name
void unmap_all(const char *name);

// Get library name and base address that contains the function
uintptr_t get_function_lib(uintptr_t addr, char *lib);

// Get library base address with name
uintptr_t get_remote_lib(int pid, const char *lib);

void self_unload();
void hook_functions();
bool unhook_functions();
