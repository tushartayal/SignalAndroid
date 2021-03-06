package org.thoughtcrime.securesms.mms;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.iceteck.silicompressorr.SiliCompressor;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader.DecryptableUri;
import org.thoughtcrime.securesms.util.BitmapDecodingException;
import org.thoughtcrime.securesms.util.BitmapUtil;
import org.thoughtcrime.securesms.util.MediaUtil;
import org.thoughtcrime.securesms.util.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Set;

public abstract class MediaConstraints {
  private static final String TAG = MediaConstraints.class.getSimpleName();

  public static MediaConstraints getPushMediaConstraints() {
    return new PushMediaConstraints();
  }

  public static MediaConstraints getMmsMediaConstraints(int subscriptionId) {
    return new MmsMediaConstraints(subscriptionId);
  }

  public abstract int getImageMaxWidth(Context context);
  public abstract int getImageMaxHeight(Context context);
  public abstract int getImageMaxSize(Context context);

  public abstract int getGifMaxSize(Context context);
  public abstract int getVideoMaxSize(Context context);
  public abstract int getAudioMaxSize(Context context);
  public abstract int getDocumentMaxSize(Context context);

  public boolean isSatisfied(@NonNull Context context, @NonNull MasterSecret masterSecret, @NonNull Attachment attachment) {
    try {
      return (MediaUtil.isGif(attachment)    && attachment.getSize() <= getGifMaxSize(context)   && isWithinBounds(context, masterSecret, attachment.getDataUri())) ||
             (MediaUtil.isImage(attachment)  && attachment.getSize() <= getImageMaxSize(context) && isWithinBounds(context, masterSecret, attachment.getDataUri())) ||
             (MediaUtil.isAudio(attachment)  && attachment.getSize() <= getAudioMaxSize(context)) ||
             (MediaUtil.isVideo(attachment)  && attachment.getSize() <= getVideoMaxSize(context)) ||
             (MediaUtil.isFile(attachment) && attachment.getSize() <= getDocumentMaxSize(context));
    } catch (IOException ioe) {
      Log.w(TAG, "Failed to determine if media's constraints are satisfied.", ioe);
      return false;
    }
  }

  public boolean satisfiesCompression(@NonNull Context context, Attachment attachment, Set<String> compressionOptions) {
    return ((MediaUtil.isVideo(attachment) && compressionOptions.contains("video")) ||
            (MediaUtil.isImage(attachment) && compressionOptions.contains("image")) ||
            (MediaUtil.isGif(attachment)) && compressionOptions.contains("gif"));
  }

  private boolean isWithinBounds(Context context, MasterSecret masterSecret, Uri uri) throws IOException {
    try {
      InputStream is = PartAuthority.getAttachmentStream(context, masterSecret, uri);
      Pair<Integer, Integer> dimensions = BitmapUtil.getDimensions(is);
      return dimensions.first  > 0 && dimensions.first  <= getImageMaxWidth(context) &&
             dimensions.second > 0 && dimensions.second <= getImageMaxHeight(context);
    } catch (BitmapDecodingException e) {
      throw new IOException(e);
    }
  }

  public boolean canResize(@Nullable Attachment attachment) {
    return attachment != null && MediaUtil.isImage(attachment) && !MediaUtil.isGif(attachment);
  }

  public MediaStream getResizedMedia(@NonNull Context context,
                                     @NonNull MasterSecret masterSecret,
                                     @NonNull Attachment attachment)
      throws IOException
  {
    if (!canResize(attachment)) {
      throw new UnsupportedOperationException("Cannot resize this content type");
    }

    try {
      // XXX - This is loading everything into memory! We want the send path to be stream-like.
      return new MediaStream(new ByteArrayInputStream(BitmapUtil.createScaledBytes(context, new DecryptableUri(masterSecret, attachment.getDataUri()), this)),
                             MediaUtil.IMAGE_JPEG);
    } catch (BitmapDecodingException e) {
      throw new IOException(e);
    }
  }

 public MediaStream compressFile(@NonNull Context context, @NonNull MasterSecret masterSecret, @NonNull Attachment attachment) throws IOException, URISyntaxException {
   if (!MediaUtil.isVideo(attachment) && !MediaUtil.isImage(attachment) && !MediaUtil.isGif(attachment)) {
           throw new UnsupportedOperationException("File type not video or image or gif! Cannot compress");
   }

   String directory = context.getCacheDir().toString() + "/tempFile" + attachment.getContentType().replace('/', '.');
   InputStream is = PartAuthority.getAttachmentStream(context, masterSecret, attachment.getDataUri());
   byte[] byteStream = Util.readFully(is);

   FileOutputStream outputStream = new FileOutputStream(directory);
   outputStream.write(byteStream);

   String filepath = "";

   if(MediaUtil.isVideo(attachment)) {
     filepath = compressVideo(context, directory);
   } else {
     filepath = compressImage(context, directory);
   }

   outputStream.close();
   is.close();
   new File(directory).delete();

   return new MediaStream(new FileInputStream(filepath), attachment.getContentType());
 }

 public String compressVideo(@NonNull Context context, String directory) throws URISyntaxException, FileNotFoundException {
    return SiliCompressor.with(context).compressVideo(directory ,context.getCacheDir().toString());
  }

  public String compressImage(@NonNull Context context, String directory) throws URISyntaxException, FileNotFoundException {
    return SiliCompressor.with(context).compress(directory ,context.getCacheDir());
  }
}
