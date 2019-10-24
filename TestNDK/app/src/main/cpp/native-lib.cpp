#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL // 相当于jstring，用于指明函数的返回值类型,自己添加新函数时不要忘了需要指明返回值类型
Java_com_tangpeng_testndk_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
