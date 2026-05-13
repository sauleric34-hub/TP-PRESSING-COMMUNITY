package com.pressing.gestion_pressing.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;

public class PdfSignatureUtil implements SignatureInterface {

    private PrivateKey privateKey;
    private Certificate[] certificateChain;

    public PdfSignatureUtil(KeyStore keystore, String password, String alias) throws Exception {
        this.privateKey = (PrivateKey) keystore.getKey(alias, password.toCharArray());
        this.certificateChain = keystore.getCertificateChain(alias);
    }

    @Override
    public byte[] sign(InputStream content) throws java.io.IOException {
        try {
        CMSProcessableByteArray input = new CMSProcessableByteArray(content.readAllBytes());
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        
        X509Certificate cert = (X509Certificate) certificateChain[0];
        ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
        
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, cert));
        
        gen.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));
        
        CMSSignedData signedData = gen.generate(input, false);
        return signedData.getEncoded();
        } catch (Exception e) {
            throw new java.io.IOException(e);
        }
    }

    public static byte[] signPdf(byte[] pdfBytes, InputStream keystoreStream, String keystorePassword, String alias) throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(keystoreStream, keystorePassword.toCharArray());

        PdfSignatureUtil signatureUtil = new PdfSignatureUtil(keystore, keystorePassword, alias);

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("Pressing Community");
            signature.setLocation("Douala, Cameroun");
            signature.setReason("Facture officielle numérique");
            signature.setSignDate(Calendar.getInstance());

            document.addSignature(signature, signatureUtil);
            
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.saveIncremental(output);
            return output.toByteArray();
        }
    }
}
