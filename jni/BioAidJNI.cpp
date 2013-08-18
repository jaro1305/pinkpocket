#include <jni.h>
#include "include/BioAidJNI.hpp"

#include <boost/scoped_array.hpp>
#include <boost/thread/mutex.hpp>

#include "params.hpp"
#include "algoComponents.hpp"
#include "algoInterface.hpp"
#include "opensl_io.h"

#define BUFFERFRAMES 1024
#define VECSAMPS_MONO 64
#define VECSAMPS_STEREO 128
#define SR 44100

#define BIOAID_ON 0

static int on;

extern "C" {
void showData(const float* L, const float*R, int numel);
int main();
void start_process();
void stop_process();
}

JNIEXPORT jstring JNICALL Java_com_soundbyte_app_MainActivity_startProcessing(
		JNIEnv *env, jobject thisObj) {
	start_process();
	return env->NewStringUTF("Start");
}

JNIEXPORT jstring JNICALL Java_com_soundbyte_app_MainActivity_stopProcessing(
		JNIEnv *env, jobject thisObj) {
	stop_process();
	return env->NewStringUTF("Stop");
}

void start_process() {
	// Set up the params
	cSharedStereoParams sharedPars;
	cUniqueStereoParams leftPars;
	assert(!leftPars.setParam("OutputGain_dB", 6.f));
	assert(!leftPars.setParam("InputGain_dB", 6.f));
	assert(!leftPars.setParam("Band_3_Gain_dB", 10.f));
	assert(!sharedPars.setParam("NumBands", 4.f));
	cAidAlgo myAlgo(leftPars, sharedPars); //Supply with identical LR pars

	// Set up the input stream
	OPENSL_STREAM *p;
	p = android_OpenAudioDevice(SR, 1, 2, BUFFERFRAMES);
	if (p == NULL)
		return;

	// Main loop
	float inbuffer[VECSAMPS_MONO], outbuffer[VECSAMPS_STEREO];
	on = 1;
	while (on == 1) {
		int samps = android_AudioIn(p, inbuffer, VECSAMPS_MONO);
		if (BIOAID_ON == 1) {
			// Get the input data
			boost::scoped_array<float> lDataIn(new float[samps]());
			boost::scoped_array<float> rDataIn(new float[samps]());
			boost::scoped_array<float> lDataOut(new float[samps]());
			boost::scoped_array<float> rDataOut(new float[samps]());
			float *plDataIn = lDataIn.get();
			float *prDataIn = rDataIn.get();
			for (int i = 0; i < samps; i++) {
				*plDataIn = inbuffer[i];
				plDataIn++;
				*prDataIn = inbuffer[i];
				prDataIn++;
			}
			float* in2D[] = { plDataIn, prDataIn };

			// Get the output data
			float *plDataOut = lDataOut.get();
			float *prDataOut = rDataOut.get();
			float* out2D[] = { plDataOut, prDataOut };
			myAlgo.processSampleBlock((const float**) in2D, 1, (float**) out2D,
					2, samps);

			// Copy the output data to the output buffers
			for (int i = 0; i < samps; i++) {
				outbuffer[(i * 2)] = *plDataOut;
				plDataOut++;
				outbuffer[(i * 2) + 1] = *prDataOut;
				prDataOut++;
			}
		} else {
			// Directly copy the input buffer to the output buffer
			for (int i = 0; i < samps; i++) {
				outbuffer[(i * 2)] = outbuffer[(i * 2) + 1] = inbuffer[i];
			}
		}
		android_AudioOut(p, outbuffer, samps * 2);
	}
	android_CloseAudioDevice(p);
}

void stop_process() {
	on = 0;
}

// vis helper
void showData(const float* L, const float*R, int numel) {
	for (int nn = 0; nn < numel; ++nn)
		std::cout << "L[" << nn << "]=" << L[nn] << "  ----   " << "R[" << nn
				<< "]=" << R[nn] << std::endl;
}

//Random float range generating functor
//call with std::generate_n(x.begin(), num_items, gen_rand(min,max));
//or with std::generate_n(std::back_inserter(x), num_items, gen_rand(min,max));
struct gen_rand {
	float range, factor, norm;
public:
	gen_rand(float mi = 0.f, float ma = 1.f) :
			range(ma - mi), factor(range / RAND_MAX), norm(mi) {
	}
	float operator()() {
		return rand() * factor + norm;
	}
};

//------MAIN-----
int main() {
	//DBGM(RAND_MAX);
	int numSamples = 10;

	// the () at the end = zero init like calloc
	boost::scoped_array<float> lDataIn(new float[numSamples]());
	boost::scoped_array<float> rDataIn(new float[numSamples]());
	boost::scoped_array<float> lDataOut(new float[numSamples]());
	boost::scoped_array<float> rDataOut(new float[numSamples]());

	lDataIn[0] = rDataIn[0] = 1.0f; // make impulse
	//std::generate_n(&lDataIn[0], numSamples, gen_rand(10.f,11.f));

	// Make the data look like 2D C-style arrays (the process mathod requires data in this format)
	// ..this fits with other audio APIs like VST, even if it is a bit of an eye-bleeder
	float *plDataIn = lDataIn.get();
	float *prDataIn = rDataIn.get();
	float *plDataOut = lDataOut.get();
	float *prDataOut = rDataOut.get();
	float* in2D[] = { plDataIn, prDataIn };
	float* out2D[] = { plDataOut, prDataOut };

	showData(lDataIn.get(), rDataIn.get(), numSamples);

	{
		//      _                        _
		//   __| | ___ _ __ ___   ___   / |
		//  / _` |/ _ \ '_ ` _ \ / _ \  | |
		// | (_| |  __/ | | | | | (_) | | |
		//  \__,_|\___|_| |_| |_|\___/  |_|
		// Stereo  in/out

		DBGM(std::endl << std::endl << "Demo 1" << std::endl << std::endl);

		// Creating and passing in a Mutex is useless in this (single threaded) demo, but it demostrates the syntax.
		// It is fine to just omit the arg (see demo_2).
		boost::mutex myMutex;

		cSharedStereoParams sharedPars(&myMutex);
		cUniqueStereoParams leftPars(&myMutex);
		cUniqueStereoParams rightPars(&myMutex);
		cAidAlgo myAlgo(leftPars, rightPars, sharedPars, &myMutex); //Supply with identical LR pars

		myAlgo.processSampleBlock((const float**) in2D, 2, (float**) out2D, 2,
				numSamples);
		showData(lDataOut.get(), rDataOut.get(), numSamples);

		// Now change a parameter in one channel, process and see the new output
		assert(!rightPars.setParam("OutputGain_dB", 6.f)); // Returns a bool letting you know if the parameter you're trying to set exists. Need to replace with exception.
		myAlgo.processSampleBlock((const float**) in2D, 2, (float**) out2D, 2,
				numSamples);
		showData(lDataOut.get(), rDataOut.get(), numSamples);
	}DBGM(std::endl << std::endl << "Demo 1 END" << std::endl << std::endl);

	// Use some randome numbers for the left channel instead ...
	std::generate_n(&lDataIn[0], numSamples, gen_rand(-1.f, 1.f));
	showData(lDataIn.get(), rDataIn.get(), numSamples);

	{
		//      _                        ____
		//   __| | ___ _ __ ___   ___   |___ \
           //  / _` |/ _ \ '_ ` _ \ / _ \    __) |
		// | (_| |  __/ | | | | | (_) |  / __/
		//  \__,_|\___|_| |_| |_|\___/  |_____|
		// Mono in with dual out
		DBGM(std::endl << std::endl << "Demo 2" << std::endl << std::endl);
		cSharedStereoParams sharedPars;
		cUniqueStereoParams leftPars;
		assert(!leftPars.setParam("OutputGain_dB", 6.f));
		assert(!leftPars.setParam("InputGain_dB", 6.f));
		assert(!leftPars.setParam("Band_3_Gain_dB", 10.f));
		assert(!sharedPars.setParam("NumBands", 4.f));
		cAidAlgo myAlgo(leftPars, sharedPars); //Supply with identical LR pars
		myAlgo.processSampleBlock((const float**) in2D, 1, (float**) out2D, 2,
				numSamples);
		showData(lDataOut.get(), rDataOut.get(), numSamples);
	}DBGM(std::endl << std::endl << "Demo 2 END" << std::endl << std::endl);

#if defined _WIN32 || defined _WIN64    // Just waits for user input before vanishing
	std::string xyz;
	std::cin >> xyz;
#endif
}
