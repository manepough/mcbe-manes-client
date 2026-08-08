cmake_minimum_required(VERSION 3.22)
project(mcbe_client CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(mcbe_client SHARED
    client/main.cpp
    client/hooks/egl_hook.cpp
    client/modules/hud/fps.cpp
    client/modules/hud/coordinates.cpp
    client/modules/visual/fullbright.cpp
    client/modules/utility/fps_boost.cpp
)

target_include_directories(mcbe_client PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

target_compile_options(mcbe_client PRIVATE
    -O2
    -fvisibility=hidden
    -fno-exceptions
    -fno-rtti
    -march=armv8-a
)

target_link_libraries(mcbe_client
    android
    EGL
    GLESv2
    log
    dl
)
