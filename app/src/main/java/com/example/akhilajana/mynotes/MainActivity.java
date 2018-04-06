package com.example.akhilajana.mynotes;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mInputText, mDocName, mInputTitle;
    private Button mCreatePdfButton;
    private File pdfFile;
    private ImageButton mAttachImg, btnBold,btnItalic, btnUnderline;
    private Document document;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 100;
    private static final String TAG = "PdfCreatorActivity";

    private String flag="null";

    private int selectionStart,selectionEnd;
    private String startingText,endingText, selectedText;


//    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        document = new Document();
 //       mStorageRef = FirebaseStorage.getInstance().getReference();

        mDocName = (EditText) findViewById(R.id.edit_file_name);
        mInputTitle = (EditText) findViewById(R.id.edit_title);
        mInputText = (EditText) findViewById(R.id.edit_text_content);
        mCreatePdfButton = (Button) findViewById(R.id.button_create);
       // mAttachImg = (ImageView) findViewById(R.id.attach_Img);
        btnBold = findViewById(R.id.bold_Img);
        btnItalic = findViewById(R.id.italic_Img);
        btnUnderline = findViewById(R.id.underline_Img);

        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        mCreatePdfButton.setOnClickListener(this);
//        mAttachImg.setOnClickListener(this);

        btnBold.setOnClickListener(this);
        btnItalic.setOnClickListener(this);
        btnUnderline.setOnClickListener(this);

    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
//            case R.id.attach_Img:
//
//                chooseImage();
//
//                break;

            case R.id.button_create:

                try {
                    validateInput();
                    createPdfWrapper();
                }
                catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                catch (DocumentException e) {
                        e.printStackTrace();
                    }
                break;

            case R.id.bold_Img:

                if(!document.isOpen())
                document.open();

                selectionStart = mInputText.getSelectionStart();
                selectionEnd = mInputText.getSelectionEnd();

                startingText = mInputText.getText().toString().substring(0, selectionStart);
                selectedText = mInputText.getText().toString().substring(selectionStart, selectionEnd);
                endingText = mInputText.getText().toString().substring(selectionEnd);

                mInputText.setText(Html.fromHtml(startingText + "<b>" + selectedText + "</b>" + endingText));
                flag="bold";

                break;

            case R.id.italic_Img:

                if(!document.isOpen())
                    document.open();

                selectionStart = mInputText.getSelectionStart();
                selectionEnd = mInputText.getSelectionEnd();

                startingText = mInputText.getText().toString().substring(0, selectionStart);
                selectedText = mInputText.getText().toString().substring(selectionStart, selectionEnd);
                endingText = mInputText.getText().toString().substring(selectionEnd);

                mInputText.setText(Html.fromHtml(startingText + "<i>" + selectedText + "</i>" + endingText));
                flag="italic";

                break;

            case R.id.underline_Img:

                if(!document.isOpen())
                    document.open();

                selectionStart = mInputText.getSelectionStart();
                selectionEnd = mInputText.getSelectionEnd();

                startingText = mInputText.getText().toString().substring(0, selectionStart);
                selectedText = mInputText.getText().toString().substring(selectionStart, selectionEnd);
                endingText = mInputText.getText().toString().substring(selectionEnd);

                mInputText.setText(Html.fromHtml(startingText + "<u>" + selectedText + "</u>" + endingText));
                flag="underline";

                break;

        }

    }

    private void validateInput()
    {
        if (mInputText.getText().toString().isEmpty()){
            mInputText.setError("Body is empty");
            mInputText.requestFocus();
            return;
        }
        else if (mInputTitle.getText().toString().isEmpty()){
            mInputTitle.setError("Title is empty");
            mInputTitle.requestFocus();
            return;
        }
        else if(mDocName.getText().toString().isEmpty())
        {
            mDocName.setError("Document name is empty");
            mDocName.requestFocus();
            return;
        }
    }

    private void createPdfWrapper() throws FileNotFoundException,DocumentException
    {

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS))
                {
                    showMessageOKCancel("You need to allow access to Storage", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        }
                    });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }
        else
        {
            try {
                createPdf();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showMessageOKCancel(String msg, DialogInterface.OnClickListener onClickListener) {

        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("OK", onClickListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Permission Granted
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    // Permission Denied
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void createPdf() throws IOException, DocumentException
    {
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(TAG, "Created a new directory for PDF");
        }

        pdfFile = new File(docsFolder.getAbsolutePath(), mDocName.getText().toString());
        OutputStream output = new FileOutputStream(pdfFile);
        PdfWriter.getInstance(document, output);

        Font f=new Font(Font.FontFamily.TIMES_ROMAN,30.0f,Font.BOLD, BaseColor.BLACK);
        Paragraph titleParagraph=new Paragraph(mInputTitle.getText().toString(),f);

        titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);

        document.open();

        document.add(titleParagraph);
        document.setMargins(1,1,1,1);

        if(flag.equals("bold"))
        {
            //Format bold
            Font f1=new Font(Font.FontFamily.TIMES_ROMAN,10.0f,Font.BOLD, BaseColor.BLACK);
            Paragraph b1=new Paragraph(selectedText,f1);
            document.add(new Paragraph(startingText));
            document.add(b1);
            document.add(new Paragraph(endingText));
        }
        else if(flag.equals("italic"))
        {
            //Format italic
            Font f1=new Font(Font.FontFamily.TIMES_ROMAN,10.0f,Font.ITALIC, BaseColor.BLACK);
            Paragraph b1=new Paragraph(selectedText,f1);
            document.add(new Paragraph(startingText));
            document.add(b1);
            document.add(new Paragraph(endingText));
        }
        else if(flag.equals("underline"))
        {
            //Format underline
            Font f1=new Font(Font.FontFamily.TIMES_ROMAN,10.0f,Font.UNDERLINE, BaseColor.BLACK);
            Paragraph b1=new Paragraph(selectedText,f1);
            document.add(new Paragraph(startingText));
            document.add(b1);
            document.add(new Paragraph(endingText));
        }
        else
        {
            document.add(new Paragraph(mInputText.getText().toString()));

        }

        //addImagetoPDF();

        document.close();
        promptForNextAction();
    }


    /**After pdf is created**/
    private void promptForNextAction()
    {
        final String[] options = { "Email", "Preview", "Cancel" };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Note Saved, What Next?");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (options[which].equals("Email")){
                        emailNote();
                    }else if (options[which].equals("Preview")){
                        previewPdf();
                    }else if (options[which].equals("Cancel")){
                        dialog.dismiss();
                    }
                }
            });

            builder.show();
    }

    private void emailNote()
    {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT,"subject");
        email.putExtra(Intent.EXTRA_TEXT, "PFA");
        Uri uri = Uri.fromFile(pdfFile);
        email.putExtra(Intent.EXTRA_STREAM, uri);
        email.setType("application/pdf");
        startActivity(email);
    }

    private void previewPdf()
    {

        Uri path = Uri.fromFile(pdfFile);
        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfOpenintent.setDataAndType(path, "application/pdf");
        try {
            startActivity(pdfOpenintent);
        }
        catch (ActivityNotFoundException e) {

        }
    }


/*Image enhancement

 private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            uploadImagetoFirebase();
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//
//                //imageView.setImageBitmap(bitmap);
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
        }
    }

    private void uploadImagetoFirebase() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = mStorageRef.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    public void addImagetoPDF() throws IOException, DocumentException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.pdfimage);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , stream);
        Image myImg = Image.getInstance(stream.toByteArray());
        myImg.setAlignment(Image.MIDDLE);

        //add image to document
        document.add(myImg);
        mAttachImg.setImageResource(R.drawable.ic_plus_one_black_24dp);
    }*/


}
