package linhtm.test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

/**
 * Created by linhtm on 11/4/15.
 */
public class IM4Java {
     private static final String OS_AUTH_URL = System.getenv("OS_AUTH_URL");
     private static final String OS_TENANT_NAME = System.getenv("OS_TENANT_NAME");
     private static final String OS_TENANT_ID = System.getenv("OS_TENANT_ID");
     private static final String OS_USERNAME = System.getenv("OS_USERNAME");
     private static final String OS_PASSWORD = System.getenv("OS_PASSWORD");
     private static final String OS_IMAGE_CONTAINER = System.getenv("OS_IMAGE_CONTAINER");

    public static void main(String[] args) {
        try {
            String srcImageUrl = args[0];
            Boolean isToCND =  (Integer.parseInt(args[1]) != 0);
            int lastDot = srcImageUrl.lastIndexOf('.');
            String rawImageName = srcImageUrl.substring(srcImageUrl.lastIndexOf('/'));
            String imageName = rawImageName.substring(1, rawImageName.lastIndexOf('.'));
            String suffix = srcImageUrl.substring(lastDot);
            String dstImage = imageName + "_small" + suffix;

            URL imageUrl = new URL(srcImageUrl);
            URLConnection conn = imageUrl.openConnection();
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            // create command
            ConvertCmd cmd = new ConvertCmd();

            Pipe inPipe = new Pipe(is, null);
            Pipe outPipe = new Pipe(null, os);

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage("-");
            op.quality(80.0);
            op.coalesce();
            op.resize(300, 300, '!');
            op.addImage(suffix.substring(1) + ":-");


            cmd.setInputProvider(inPipe);
            cmd.setOutputConsumer(outPipe);
            cmd.run(op);
            is.close();
            os.close();

            if (isToCND) {
                ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());
                Account osAccount = new AccountFactory()
                        .setUsername(OS_USERNAME)
                        .setPassword(OS_PASSWORD)
                        .setAuthUrl(OS_AUTH_URL)
                        .setTenantName(OS_TENANT_NAME)
                        .setTenantId(OS_TENANT_ID)
                        .createAccount();

                Container container = osAccount.getContainer(OS_IMAGE_CONTAINER);
                StoredObject object = container.getObject(dstImage);
                object.uploadObject(bis);
            } else {
                OutputStream outputStream = new FileOutputStream(dstImage);
                os.writeTo(outputStream);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
