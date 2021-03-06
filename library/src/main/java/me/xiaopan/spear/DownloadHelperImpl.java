/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.spear;

import android.util.Log;

/**
 * DownloadHelper
 */
public class DownloadHelperImpl implements DownloadHelper{
    private static final String NAME = "DownloadHelperImpl";

    // 基本属性
    protected Spear spear;
    protected String uri;
    protected String name;

    // 下载属性
    protected boolean enableDiskCache = true;
    protected ProgressListener progressListener;
    protected DownloadListener downloadListener;

    /**
     * 创建下载请求生成器
     * @param spear Spear
     * @param uri 图片Uri，支持以下几种
     * <blockquote>“http://site.com/image.png“  // from Web
     * <br>“https://site.com/image.png“ // from Web
     * </blockquote>
     */
    public DownloadHelperImpl(Spear spear, String uri) {
        this.spear = spear;
        this.uri = uri;
    }

    @Override
    public DownloadHelperImpl name(String name){
        this.name = name;
        return this;
    }

    @Override
    public DownloadHelperImpl listener(DownloadListener downloadListener){
        this.downloadListener = downloadListener;
        return this;
    }

    @Override
    public DownloadHelperImpl disableDiskCache() {
        this.enableDiskCache = false;
        return this;
    }

    @Override
    public DownloadHelperImpl progressListener(ProgressListener progressListener){
        this.progressListener = progressListener;
        return this;
    }

    @Override
    public DownloadHelperImpl options(DownloadOptions options){
        if(options == null){
            return this;
        }

        if(!options.isEnableDiskCache()){
            this.enableDiskCache = false;
        }

        return this;
    }

    @Override
    public DownloadHelperImpl options(Enum<?> optionsName){
        return options((DownloadOptions) Spear.getOptions(optionsName));
    }

    @Override
    public Request fire(){
        if(downloadListener != null){
            downloadListener.onStarted();
        }

        // 验证uri参数
        if(uri == null || "".equals(uri.trim())){
            if(Spear.isDebugMode()){
                Log.e(Spear.TAG, NAME + " - " + "uri is null or empty");
            }
            if(downloadListener != null){
                downloadListener.onFailed(FailCause.URI_NULL_OR_EMPTY);
            }
            return null;
        }

        // 过滤掉不支持的URI协议类型
        UriScheme uriScheme = UriScheme.valueOfUri(uri);
        if(uriScheme == null){
            if(Spear.isDebugMode()){
                Log.e(Spear.TAG, NAME + " - " + "unknown uri scheme" + " - " + uri);
            }
            if(downloadListener != null){
                downloadListener.onFailed(FailCause.URI_NO_SUPPORT);
            }
            return null;
        }

        if(!(uriScheme == UriScheme.HTTP || uriScheme == UriScheme.HTTPS)){
            if(Spear.isDebugMode()){
                Log.e(Spear.TAG, NAME + " - " + "only support http ot https" + " - " + uri);
            }
            if(downloadListener != null){
                downloadListener.onFailed(FailCause.URI_NO_SUPPORT);
            }
            return null;
        }

        // 创建请求
        DownloadRequest request = spear.getConfiguration().getRequestFactory().newDownloadRequest(spear, uri, uriScheme);

        request.setName(name != null ? name : uri);
        request.setEnableDiskCache(enableDiskCache);

        request.setDownloadListener(downloadListener);
        request.setProgressListener(progressListener);

        request.postRunDispatch();

        return request;
    }
}
