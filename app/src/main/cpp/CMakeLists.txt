cmake_minimum_required(VERSION 3.22.1)
project("novafr")

# Disable native host flags and executable targets for Android cross-compilation
set(GGML_NATIVE OFF CACHE BOOL "Disable native host optimization" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "Disable llama examples" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "Disable llama tests" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "Disable llama server binary" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "Build static libraries for linking" FORCE)

# Add llama.cpp sub-project
add_subdirectory(llama.cpp)

# Define JNI library
add_library(llama_android SHARED llama_android.cpp)

# Include header directories
target_include_directories(llama_android PRIVATE 
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/include
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/ggml/include
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp
)

# Link libraries
find_library(log-lib log)
target_link_libraries(llama_android
    PRIVATE
    llama
    ggml
    ${log-lib}
)
