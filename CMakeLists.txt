# Minimal CMake wrapper for the opensplice build
cmake_minimum_required(VERSION 2.8)
project(opensplice)

# Hacky, but will work for now
math(EXPR BITS "8*${CMAKE_SIZEOF_VOID_P}")
if(APPLE)
  set(platform "x86_64.darwin10_clang-release")
elseif(WIN32)
  message(FATAL_ERROR "Windows not yet supported")
else()
  if (BITS EQUAL 64)
    set(platform "x86_64.linux-release")
  else (BITS EQUAL 64)
    set(platform "x86.linux-release")
  endif()
endif()

# To build, run our bash wrapper script
add_custom_target(build ALL
                  COMMAND ${CMAKE_CURRENT_SOURCE_DIR}/minimal_build.sh ${platform}
                  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR})

# To install, cherry-pick some stuff
install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/install/minimal/lib/
        DESTINATION lib)
install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/install/minimal/include/
        DESTINATION include)
install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/install/minimal/etc/
        DESTINATION etc)
install(PROGRAMS ${CMAKE_CURRENT_SOURCE_DIR}/install/minimal/bin/cppgen ${CMAKE_CURRENT_SOURCE_DIR}/install/minimal/bin/idlpp
        DESTINATION bin)
install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/install/minimal/share/
        DESTINATION share)

# Set up CPack to produce a source tarball.
list (APPEND CPACK_SOURCE_GENERATOR "TBZ2")
set(CPACK_PACKAGE_VERSION "6.4.0")
set(CPACK_SOURCE_PACKAGE_FILE_NAME "@PROJECT_NAME@-@CPACK_PACKAGE_VERSION@")
set(CPACK_PACKAGE_VERSION_MAJOR "6")
set(CPACK_PACKAGE_VERSION_MINOR "4")
set(CPACK_PACKAGE_VERSION_PATCH "0")
include(CPack)
