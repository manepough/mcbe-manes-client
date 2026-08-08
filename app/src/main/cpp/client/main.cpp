// ============================================================
//  main.cpp -- native client entry point
//
//  This is the skeleton. It compiles and runs right now.
//  The JNI methods return safe defaults until you fill in
//  real offsets from Ghidra reverse engineering.
//
//  How to add a real module once you have an offset:
//  1. Find the function in Ghidra, record the byte pattern
//  2. Add a signature to signatures.hpp
//  3. Call Memory::findPattern() in JNI_OnLoad
//  4. Hook or read the address
//  5. Return real data from the JNI method below
// ============================================================

#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <cstring>
#include <cstdio>
#include <atomic>
#include <pthread.h>

#define TAG  "MCBEClient"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// ============================================================
//  Shared state written by native hooks, read by JNI methods
// ============================================================
namespace State {
    std::atomic<float> playerX{0.f};
    std::atomic<float> playerY{64.f};
    std::atomic<float> playerZ{0.f};
    std::atomic<float> playerYaw{0.f};
    std::atomic<float> playerPitch{0.f};
    std::atomic<bool>  onGround{true};
    std::atomic<int>   cps{0};
    std::atomic<float> ping{0.f};
    std::atomic<bool>  fullbright{false};
    std::atomic<bool>  fpsBoost{false};
    std::atomic<bool>  noFog{false};
}

// ============================================================
//  JNI_OnLoad -- runs when System.loadLibrary() is called
// ============================================================
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("Client native library loaded");

    // When you have offsets from Ghidra, initialize hooks here.
    // Example (fill in real values):
    //
    // uintptr_t base = Memory::getModuleBase("libminecraftpe.so");
    // Hook::install(base + OFFSET_PLAYER_TICK, &playerTickHook, &origPlayerTick);

    return JNI_VERSION_1_6;
}

// ============================================================
//  JNI method implementations
//  These are called by ClientBridge.java in the overlay
// ============================================================
extern "C" {

// float[] getPlayerPosition()
JNIEXPORT jfloatArray JNICALL
Java_com_mcbe_client_ClientBridge_getPlayerPosition(JNIEnv* env, jclass) {
    jfloatArray arr = env->NewFloatArray(3);
    jfloat pos[3] = {
        State::playerX.load(),
        State::playerY.load(),
        State::playerZ.load()
    };
    env->SetFloatArrayRegion(arr, 0, 3, pos);
    return arr;
}

// float getPlayerYaw()
JNIEXPORT jfloat JNICALL
Java_com_mcbe_client_ClientBridge_getPlayerYaw(JNIEnv*, jclass) {
    return State::playerYaw.load();
}

// float getPlayerPitch()
JNIEXPORT jfloat JNICALL
Java_com_mcbe_client_ClientBridge_getPlayerPitch(JNIEnv*, jclass) {
    return State::playerPitch.load();
}

// boolean isOnGround()
JNIEXPORT jboolean JNICALL
Java_com_mcbe_client_ClientBridge_isOnGround(JNIEnv*, jclass) {
    return State::onGround.load();
}

// int getCps()
JNIEXPORT jint JNICALL
Java_com_mcbe_client_ClientBridge_getCps(JNIEnv*, jclass) {
    return State::cps.load();
}

// float getServerPing()
JNIEXPORT jfloat JNICALL
Java_com_mcbe_client_ClientBridge_getServerPing(JNIEnv*, jclass) {
    return State::ping.load();
}

// String getDimensionName()
JNIEXPORT jstring JNICALL
Java_com_mcbe_client_ClientBridge_getDimensionName(JNIEnv* env, jclass) {
    return env->NewStringUTF("Overworld");
}

// void setFullbright(boolean on)
JNIEXPORT void JNICALL
Java_com_mcbe_client_ClientBridge_setFullbright(JNIEnv*, jclass, jboolean on) {
    State::fullbright.store(on);
    // When you have the brightness offset:
    // Memory::writeMemory<float>(brightnessAddr, on ? 1.0f : originalBrightness);
    LOGI("Fullbright: %s", on ? "ON" : "OFF");
}

// void setFpsBoost(boolean on)
JNIEXPORT void JNICALL
Java_com_mcbe_client_ClientBridge_setFpsBoost(JNIEnv*, jclass, jboolean on) {
    State::fpsBoost.store(on);
    LOGI("FPS Boost: %s", on ? "ON" : "OFF");
}

// void setNoFog(boolean on)
JNIEXPORT void JNICALL
Java_com_mcbe_client_ClientBridge_setNoFog(JNIEnv*, jclass, jboolean on) {
    State::noFog.store(on);
    LOGI("No Fog: %s", on ? "ON" : "OFF");
}

} // extern "C"
