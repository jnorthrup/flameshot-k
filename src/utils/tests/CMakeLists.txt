# CMake for utils unit tests

find_package(GTest QUIET)
if (NOT GTest_FOUND)
  include(FetchContent)
  # Fetch GoogleTest as a fallback
  FetchContent_Declare(
    googletest
    GIT_REPOSITORY https://github.com/google/googletest.git
    GIT_TAG main
  )
  FetchContent_MakeAvailable(googletest)
endif()

add_executable(utils_valuehandler_tests
  valuehandler_unittests.cpp
)

target_link_libraries(utils_valuehandler_tests
  PRIVATE
    gtest_main
    Flameshot::flameshot
)

enable_testing()
add_test(NAME utils_valuehandler_tests COMMAND utils_valuehandler_tests)
