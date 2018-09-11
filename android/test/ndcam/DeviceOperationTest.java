package ndcam;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.media.ImageReader;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Future;

@RunWith(AndroidJUnit4.class)
public class DeviceOperationTest extends CameraModelTest {

    ImageReader reader;
    Device camera;

    @Before
    public void CreateImageReader() {
        // 1920 * 1080, 30 FPS, YCbCr 4:2:0(YUV_420_888)
        reader = ImageReader.newInstance(
                1920, 1080, ImageFormat.YUV_420_888,
                30 // reserve some images
        );

        Assert.assertNotNull(reader);
    }

    @Before
    public void AcquireDevice()
    {
        CameraModel.Init();
        Device[] devices = CameraModel.GetDevices();
        Assert.assertNotNull(devices);
        camera = null;
        // get rear camera
        for (Device device : devices)
            if (device.facing() == CameraCharacteristics.LENS_FACING_BACK)
                camera = device;

        Assert.assertNotNull(camera);
    }

    @After
    public void CloseDevice() throws  Exception{
        Assert.assertNotNull(camera);
        camera.close();
        // wait for camera framework to stop completely
        Thread.sleep(500);
    }

    @Test
    public void CloseWithoutStopRepeat() throws Exception {
        // start repeating capture operation
        camera.repeat(reader.getSurface());

        // Android Camera 2 API uses background thread.
        // Give some time to the framework.
        Thread.sleep(100);

        // camera.stop(); // !!! stop is missing !!!
    }

    @Test
    public void TryRepeatCapture() throws Exception{
        // start repeating capture operation
        camera.repeat(reader.getSurface());

        Thread.sleep(100);

        Image image = null;
        int i = 0, count = 0;
        while(i++ < 100) // try 100 capture (repeat mode)
        {
            // expect 30 FPS...
            Thread.sleep(30);

            // Fetch image 1 by 1.
            image = reader.acquireNextImage();
            if(image == null)
                continue;

            Log.v("ndk_camera",
                    String.format("format %d width %d height %d timestamp %d",
                            image.getFormat(), image.getWidth(), image.getHeight(),
                            image.getTimestamp())
            );
            image.close();
            count += 1;
        }
        camera.stopRepeat(); // stop after iteration

        Assert.assertNotNull(image);    // ensure at least 1 image was acquired
        Assert.assertTrue(i > count);   // !!! some images might be dropped !!!
    }

    @Test
    public void CloseWithoutStopCapture() throws Exception {
        // start repeating capture operation
        camera.repeat(reader.getSurface());

        // Android Camera 2 API uses background thread.
        // Give some time to the framework.
        Thread.sleep(100);

        // camera.stop(); // !!! stop is missing !!!
    }

    @Test
    public void TryCapture()  throws Exception{
        // start capture operation
        camera.capture(reader.getSurface());

        Thread.sleep(100);

        Image image = null;
        do
        {
            // give more time...
            Thread.sleep(30);

            image = reader.acquireLatestImage();
        }while(image == null);

        Assert.assertNotNull(image);

        Log.e("ndk_camera",
                String.format("format %d width %d height %d timestamp %d",
                        image.getFormat(), image.getWidth(), image.getHeight(),
                        image.getTimestamp())
        );
        image.close();
        camera.stopCapture(); // stop after capture
    }
}
