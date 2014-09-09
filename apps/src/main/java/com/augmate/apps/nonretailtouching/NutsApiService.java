package com.augmate.apps.nonretailtouching;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.POST;
import retrofit.http.Path;

public interface NutsApiService {
    @POST("/non_retail_pieces/{barcode}/touch.json")
    void touchNonRetailPiece(@Path("barcode") String barcode, Callback<Response> response);
}
