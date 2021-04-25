# MediaCodecEncodeH264


MediaCodec 编码 onPreviewFrame 回调的YUV数据为 h264

#2019.8.16
1.更新为android studio工程;
2.并且修复了android 6.0以上动态权限bug;
3.修复android p崩溃bug




---

~~基于daoke666的[MediaCodecEncodeH264](https://github.com/daoke666/MediaCodecEncodeH264)改造~~

基于sszhangpengfei的[MediaCodecEncodeH264](https://github.com/sszhangpengfei/MediaCodecEncodeH264)改造，以上为原作者README。

另有参考如下项目，一并表示感谢：

[yishuinanfeng/YuvVideoPlayerDemo](https://github.com/yishuinanfeng/YuvVideoPlayerDemo)

[chinghungpan/MediaCodecDecodeRawStreamToYuv](https://github.com/chinghungpan/MediaCodecDecodeRawStreamToYuv)

[Eajy/MaterialDesignDemo](https://github.com/Eajy/MaterialDesignDemo)



### 目前改动如下：

- 增加encode启停按钮
- Camera编码时添加YUV dump，默认dump生成数据格式为NV21，支持切换为NV12、I420
- 增加使用YUV文件做input Source，目前仅支持NV21格式的YUV文件，可通过右上角Button切换Source为Camera或File。
- 支持播放dump出的YUV，目前仅支持I420格式存储的YUV数据。
- 增加简易mediacodecinfo查看功能
- 基于mediacodec实现H.264与H.265解码播放，目前使用软解，dump功能有待完善。

### 开发中
- 支持切换编码解码时的分辨率，目标支持720P、1080P、3840P、
- YUV文件做input Source时。encode的同时播放YUV。
- 使用mediacodec实现H.265编码
- 实现audio录制解码打包
- 支持硬解软解切换


### 当前界面
#### 侧滑菜单
![功能菜单](https://raw.githubusercontent.com/muzhiyun/MediaCodecEncodeH264/master/Screenshot/Screenshot_20201212-152814.png)


#### mediacodecInfo
![mediacodecInfo](https://raw.githubusercontent.com/muzhiyun/MediaCodecEncodeH264/master/Screenshot/Screenshot_20201212-152820.png)

#### Camera/YUV文件编码为H.264
![Camera/YUV文件编码为H.264](https://raw.githubusercontent.com/muzhiyun/MediaCodecEncodeH264/master/Screenshot/Screenshot_20201212-152842.png)