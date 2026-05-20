package net.cc.cca.network.api;

import net.cc.cca.network.entity.VersionInfo;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Created by Stardust on 2017/9/20.
 */

public interface UpdateCheckApi {

    @GET("/assets/autojs/version.json")
    @Headers("Cache-Control: no-cache")
    Observable<VersionInfo> checkForUpdates();

}
