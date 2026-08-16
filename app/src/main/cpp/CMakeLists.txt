cmake_minimum_required(VERSION 3.22.1)
project("novafr")

add_library(llama_android SHARED llama_android.cpp)
find_library(log-lib log)
target_link_libraries(llama_android ${log-lib})
