LOCAL_PATH := $(call my-dir)
include $(call all-subdir-makefiles)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog -ldl
LOCAL_MODULE    := bioaid
LOCAL_SRC_FILES := BioAidJNI.cpp
LOCAL_CPP_FLAGS := -felide-constructors
LOCAL_STATIC_LIBRARIES := boost_atomic boost_chrono boost_context boost_coroutine boost_date_time boost_exception boost_filesystem boost_graph boost_iostreams boost_log boost_log_setup boost_math_c99 boost_math_c99f boost_math_c99l boost_math_tr1 boost_math_tr1f boost_math_tr1l boost_prg_exec_monitor boost_program_options boost_random boost_regex boost_signals boost_system boost_test_exec_monitor boost_thread boost_timer boost_unit_test_framework boost_wave

include $(BUILD_SHARED_LIBRARY)
$(call import-module,boost_1_54_0)
