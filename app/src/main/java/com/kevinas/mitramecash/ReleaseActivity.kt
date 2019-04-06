package com.kevinas.mitramecash

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.TextView
import com.kevinas.mitramecash.json.JSONObject

class ReleaseActivity: AppCompatActivity(){
    var andLib: AndroidLib = AndroidLib();
    var aes = AES();
    var loading: AlertDialog? = null;
    open fun releaseApiSucceed(data: JSONObject, nominal: String){

    }
    open fun releaseApiFail(data: JSONObject, nominal: String){

    }
    open fun releaseApiError(msg: String){

    }
    fun releaseSucceed(data: JSONObject, nominal: String){
        loading!!.dismiss();
        var dialog: AlertDialog.Builder =  andLib.dialog("Release Rp. "+ nominal as String+" Berhasil. saldo anda: Rp. " + (data.get("data") as JSONObject).get("saldoEmoney") as String, "Sukses");
        var listener0: DialogInterface.OnClickListener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                onBackPressed();
            }
        }
        dialog.setPositiveButton("Ok", listener0);
        dialog.show();
    }
    fun releaseError(msg: String){
        loading!!.dismiss();
        var dialog = andLib.dialog("Gagal", "Release  Gagal");
        var listener0: DialogInterface.OnClickListener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                onBackPressed();
            }
        }
        dialog.setPositiveButton("Ok", listener0);
        dialog.show();
    }
    fun releaseFail(data: JSONObject, nominal: String){
        loading!!.dismiss();
        var dialog = andLib.dialog("Gagal", "Release "+ ((data.get("data") as JSONObject).get("saldoEmoney") as String)+" Gagal");
        var listener0: DialogInterface.OnClickListener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                onBackPressed();
            }
        }
        dialog.setPositiveButton("Ok", listener0);
    }
    fun releaseApi(userID: String, nominal: String, apiOnly:Boolean=true): JSONObject {
        var net = object : MeCashLib() {
            override fun <ByteArray> onDone(result: ByteArray) {
                try {
                    var releaseApiReturn = JSONObject(String(aes.decrypt((result as kotlin.ByteArray))));
                    if (releaseApiReturn.get("rc") == "00") {
                        releaseApiSucceed(releaseApiReturn, nominal);
                        if (!apiOnly) {
                            releaseSucceed(releaseApiReturn, nominal);
                        }
                    } else {
                        releaseApiFail(releaseApiReturn, nominal);
                        if (!apiOnly) {
                            releaseFail(releaseApiReturn, nominal);
                        }
                    }
                } catch (e: Exception) {
                    releaseApiError(e.toString());
                    if (!apiOnly) {
                        releaseError(e.toString());
                    }
                }
            }

            override fun onError(msg: String) {
                releaseApiError(msg);
                if (!apiOnly) {
                    releaseError(msg);
                }
            }
        }

        var releaseJson = JSONObject();
        var releaseDataJson = JSONObject();
        releaseJson = releaseJson.put("KD", "REL");
        releaseJson = releaseJson.put("userid", userID);
        releaseJson = releaseJson.put("traceid", System.nanoTime());
        releaseDataJson = releaseDataJson.put("idDevice", andLib!!.getImei());
        releaseDataJson = releaseDataJson.put("nominal", nominal);
        releaseJson = releaseJson.put("data", releaseDataJson);
        
        net.andLib = andLib!!;
        net.hitMainApi(releaseJson, "", 0);

        return releaseJson;

    }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState);
        andLib = AndroidLib(this, this@ReleaseActivity);
        loading = andLib.getLoading("", "");
        setContentView(R.layout.activity_release);
        var nominal = arrayOf(25000, 50000, 1000000);
        var listView = findViewById<LinearLayout>(R.id.list_release);
        for (i: Int in nominal){
            var rowLayout = layoutInflater.inflate(R.layout.list_release_row, listView, false);
            rowLayout.setOnClickListener{
                loading!!.show();
                releaseApi(andLib.getData("userID"), i.toString(), false);
            }
            rowLayout.findViewById<TextView>(R.id.release_saldo).text = "Rp. " + i.toString();
            listView.addView(rowLayout);
        }
    }
}