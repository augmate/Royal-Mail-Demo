package com.augmate.sdk.scanner.decoder.scandit;

import android.content.Context;
import android.os.StatFs;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// TODO: add relevant copyright/license info
// reversed out of scandit sdk
public class SbSystemUtils
{
  public SbSystemUtils() {}
  
  public static int pxFromDp(Context context, int dp)
  {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int)(dp * scale + 0.5F);
  }
  
  public static String sha1Digest(String input)
  {
    try
    {
      MessageDigest sha = MessageDigest.getInstance("SHA-1");
      sha.update(input.getBytes());
      return byteArrayToHexStr(sha.digest());
    }
    catch (NoSuchAlgorithmException e)
    {
      //ScanditSDKDiagnostics diagnostics = ScanditSDKDiagnostics.getInstance();
      //diagnostics.addException("sha-1 not available", e);
    }
    return "";
  }
  
  private static String byteArrayToHexStr(byte[] data)
  {
    char[] chars = new char[data.length * 2];
    for (int i = 0; i < data.length; i++)
    {
      byte current = data[i];
      int hi = (current & 0xF0) >> 4;
      int lo = current & 0xF;
      chars[(2 * i)] = ((char)(hi < 10 ? 48 + hi : 97 + hi - 10));
      chars[(2 * i + 1)] = ((char)(lo < 10 ? 48 + lo : 97 + lo - 10));
    }
    return new String(chars);
  }
  
  public static long getAvailableSpaceInBytes(File directory)
  {
    long availableSpace = -1L;
    StatFs stat = new StatFs(directory.getPath());
    availableSpace = stat.getAvailableBlocks() * stat.getBlockSize();
    
    return availableSpace;
  }
  
  public static long getAvailableSpaceInKB(File directory)
  {
    long SIZE_KB = 1024L;
    long availableSpace = -1L;
    StatFs stat = new StatFs(directory.getPath());
    availableSpace = stat.getAvailableBlocks() * stat.getBlockSize();
    return availableSpace / 1024L;
  }
  
  public static long getAvailableSpaceInMB(File directory)
  {
    long SIZE_KB = 1024L;
    long SIZE_MB = 1048576L;
    long availableSpace = -1L;
    StatFs stat = new StatFs(directory.getPath());
    availableSpace = stat.getAvailableBlocks() * stat.getBlockSize();
    return availableSpace / 1048576L;
  }
  
  public static long getAvailableSpaceInGB(File directory)
  {
    long SIZE_KB = 1024L;
    long SIZE_GB = 1073741824L;
    long availableSpace = -1L;
    StatFs stat = new StatFs(directory.getPath());
    availableSpace = stat.getAvailableBlocks() * stat.getBlockSize();
    return availableSpace / 1073741824L;
  }
}


