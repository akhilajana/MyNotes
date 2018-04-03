package com.example.akhilajana.mynotes;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SelfNoteFragment extends Fragment implements View.OnClickListener {
    private View mRootView;
    private EditText mSubjectEditText, mBodyEditText;
    private Button mSaveButton;
    File myFile;

    public SelfNoteFragment() {
        // Required empty public constructor
    }

    public static SelfNoteFragment newInstance(){
        SelfNoteFragment fragment = new SelfNoteFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_self_note, container, false);
        mSubjectEditText = (EditText) mRootView.findViewById(R.id.edit_text_subject);
        mBodyEditText = (EditText) mRootView.findViewById(R.id.edit_text_body);
        mSaveButton = (Button) mRootView.findViewById(R.id.button_save);

        mSaveButton.setOnClickListener(this);
        return mRootView;
    }

    private void createPdf()
    {
        //create a folder
        File pdfFolder = new File( Environment.getExternalStorageDirectory()+"/Documents");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdir();
            Log.i("demo", "Pdf Directory created");
        }
        //Create time stamp
        Date date = new Date() ;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
        //create the name of the Pdf file
        myFile = new File(pdfFolder + timeStamp + ".pdf");
        OutputStream output = null;
        try {
            output = new FileOutputStream(myFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Step 1
        Document document = new Document();

        //Step 2 Get an Instance of PdfWriter
        try {
            PdfWriter.getInstance(document, output);

        //Step 3 Open the PDF Document
        document.open();

        //Step 4 Add content
        document.add(new Paragraph(mSubjectEditText.getText().toString()));
        document.add(new Paragraph(mBodyEditText.getText().toString()));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        //Step 5: Close the document
        document.close();
        promptForNextAction();
    }

    private void viewPdf(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(myFile), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void emailNote()
    {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT,mSubjectEditText.getText().toString());
        email.putExtra(Intent.EXTRA_TEXT, mBodyEditText.getText().toString());
        Uri uri = Uri.parse(myFile.getAbsolutePath());
        email.putExtra(Intent.EXTRA_STREAM, uri);
        email.setType("message/rfc822");
        startActivity(email);
    }

    private void promptForNextAction()
    {
        final String[] options = { "Email", "Preview", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Note Saved, What Next?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Email")){
                    emailNote();
                }else if (options[which].equals("Preview")){
                    viewPdf();
                }else if (options[which].equals("Cancel")){
                    dialog.dismiss();
                }
            }
        });

        builder.show();

    }

    @Override
    public void onClick(View view)
    {
        if (mSubjectEditText.getText().toString().isEmpty()){
            mSubjectEditText.setError("Subject is empty");
            mSubjectEditText.requestFocus();
            return;
        }

        if (mBodyEditText.getText().toString().isEmpty()){
            mBodyEditText.setError("Body is empty");
            mBodyEditText.requestFocus();
            return;
        }

        createPdf();
    }
}
