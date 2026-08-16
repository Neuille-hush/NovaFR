cmake_minimum_required(VERSION 3.22.1)
project("novafr")

# 1. Include the directory containing llama.h
add_subdirectory(llama.cpp) 

# 2. Add your native bridge file
add_library(llama_android SHARED llama_android.cpp)

# 3. Include directories for header lookups
target_include_directories(llama_android PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp)

# 4. Link llama library and Android logging
find_library(log-lib log)
target_link_libraries(llama_android llama ${log-lib})
