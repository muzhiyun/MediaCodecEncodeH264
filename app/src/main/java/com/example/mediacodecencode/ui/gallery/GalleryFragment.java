package com.example.mediacodecencode.ui.gallery;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import com.example.mediacodecencode.R;

import java.util.Arrays;

public class GalleryFragment extends Fragment {
    String res = "";
    private GalleryViewModel galleryViewModel;
    String TAG = "MediacodecInfo";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        final TextView textView = root.findViewById(R.id.textView2);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText("MediacodeInfo");
                textView.append(SupportAvcCodec());
            }
        });
        return root;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private String SupportAvcCodec() {
        if (Build.VERSION.SDK_INT >= 18) {
            String string = "";
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {

                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                String[] types = codecInfo.getSupportedTypes();

                for (int i = 0; i < types.length; i++) {
                    MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(types[i]);
                    Log.e(TAG, " codecName = " + codecInfo.getName() + "\n SupportedTypes = " + Arrays.toString(types));

                    if ((types[i].substring(0,  types[i].indexOf("/"))).equalsIgnoreCase("video")) {
/*                        int level = capabilities.profileLevels[0].level;
                        int maxSupportedInstances = capabilities.getMaxSupportedInstances();
                        Log.e(TAG, level +"  " + maxSupportedInstances + "  " + capabilities.profileLevels[0].profile + "  "+capabilities.colorFormats[0]);*/

                        MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
                        string = "codecName = " + codecInfo.getName()
                                + "\n SupportedTypes = " + Arrays.toString(types)
                                + "\n SupportedHeights = " + videoCapabilities.getSupportedHeights()
                                + "\n SupportedWidths = " + videoCapabilities.getSupportedWidths()
                                + "\n BitrateRange = " + videoCapabilities.getBitrateRange()
                                + "\n SupportedFrameRates = " + videoCapabilities.getSupportedFrameRates();
                        //Log.e(TAG, string);
                        res = res + "\n\n" + string;


                    }
                    else //audio code
                    {
                        MediaCodecInfo.AudioCapabilities audioCapabilities = capabilities.getAudioCapabilities();
                        string = "codecName = " + codecInfo.getName()
                                + "\n SupportedTypes = " + Arrays.toString(types)
                                + "\n MaxInputChannelCount = " + audioCapabilities.getMaxInputChannelCount()
                                + "\n SupportedSampleRateRanges = " + Arrays.toString(audioCapabilities.getSupportedSampleRateRanges())
                                + "\n BitrateRange = " + audioCapabilities.getBitrateRange();
                        //Log.e(TAG, string);
                        res = res + "\n\n" + string;
                    }
                }
            }
            return res;
        }
        return res;
    }
}