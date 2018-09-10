package ndcam;

import android.Manifest;
import android.media.Image;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CameraModelTest
{
    @Rule
    public GrantPermissionRule cameraPermission =
            GrantPermissionRule.grant(Manifest.permission.CAMERA);

    Device[] devices;

    @Before
    public void TryInit() {
        CameraModel.Init();
    }

    @Test
    public void GetCameraDevices()
    {
        devices = CameraModel.GetDevices();
        Assert.assertNotNull(devices);
        Assert.assertTrue(devices.length > 0);

        for(Device device : devices)
        {
            Assert.assertTrue(device.id != -1);
            Assert.assertTrue(
                    device.isFront() ||
                            device.isBack() ||
                            device.isExternal()
            );
        }
    }


    @Test
    public void CaptureWithDevice()
    {
        Device camera = null;
        for(Device device : CameraModel.GetDevices())
        {
            Assert.assertTrue(device.id != -1);
            if(device.isBack())
                camera = device;
        }
        Assert.assertNotNull(camera);

        camera.capture();
    }

}