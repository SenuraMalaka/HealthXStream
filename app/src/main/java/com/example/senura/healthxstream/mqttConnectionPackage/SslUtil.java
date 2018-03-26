package com.example.senura.healthxstream.mqttConnectionPackage;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by senura on 3/26/18.
 */

public class SslUtil {



    private static final String TAG = "SSLUtilClass";

    private Context ctxt = null;

    public SslUtil(Context passedCtxt) {
        this.ctxt = passedCtxt;
    }

    public SSLSocketFactory getSocketFactory(final String password) {

        Log.d(TAG, "called the sslsocketfactory SEN");

        try {

            /**
             * Add BouncyCastle as a Security Provider
             */
            Security.addProvider(new BouncyCastleProvider());

            JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider("BC");



            /**
             * Load Certificate Authority (CA) certificate
             */

            AssetManager am = ctxt.getAssets();
            InputStream is = am.open("eb9a29fc1e54cef9c35820c9faa28c9f.7z");

            Reader targetReader = new InputStreamReader(is);

            //PEMParser reader = new PEMParser(new FileReader(caCrtFile.getFileDescriptor()));
            PEMParser reader = new PEMParser(targetReader);
            Log.d(TAG, "Parse completed - CA file");

            X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
            reader.close();


            X509Certificate caCert = certificateConverter.getCertificate(caCertHolder);




            /**
             * Load client certificate
             */

            is = am.open("85a12a388882ae92754786070457471c.7z");
            targetReader = new InputStreamReader(is);

            reader = new PEMParser(targetReader);
            X509CertificateHolder certHolder = (X509CertificateHolder) reader.readObject();
            reader.close();

            Log.d(TAG, "Read completed - CERT file");

            X509Certificate cert = certificateConverter.getCertificate(certHolder);





            /**
             * Load client private key
             */

            is = am.open("16b3a1a4294db861dea3e3e49e7e5474.7z");
            targetReader = new InputStreamReader(is);

            reader = new PEMParser(targetReader);
            Object keyObject = reader.readObject();
            reader.close();

            Log.d(TAG, "Read completed - PVTKEY file");

            PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
            JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider("BC");

            KeyPair key;

            if (keyObject instanceof PEMEncryptedKeyPair) {
                key = keyConverter.getKeyPair(((PEMEncryptedKeyPair) keyObject).decryptKeyPair(provider));
            } else {
                key = keyConverter.getKeyPair((PEMKeyPair) keyObject);
            }

            /**
             * CA certificate is used to authenticate server
             */
            KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            caKeyStore.load(null, null);
            caKeyStore.setCertificateEntry("ca-certificate", caCert);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(caKeyStore);

            /**
             * Client key and certificates are sent to server so it can authenticate the client
             */
            KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            clientKeyStore.load(null, null);
            clientKeyStore.setCertificateEntry("certificate", cert);
            clientKeyStore.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                    new Certificate[]{cert});

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());

            /**
             * Create SSL socket factory
             */
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);


            //android 4.0 support..- Sen
            SSLSocketFactory contextWithOnlyTLS = new NoSSLv3SocketFactory(context.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(contextWithOnlyTLS);



            /**
             * Return the newly created socket factory object
             */

            //contextWithOnlyTLS2=contextWithOnlyTLS;

            return contextWithOnlyTLS;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }





}
