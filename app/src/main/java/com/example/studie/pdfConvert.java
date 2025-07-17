package com.example.studie;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class pdfConvert extends AppCompatActivity {

    private EditText editText;

    private ActivityResultLauncher<Intent> createPdfLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pdf_convert);

        editText = findViewById(R.id.editTextPdfInput);

        //nag initialize ug file picker
        filePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            String mimeType = getContentResolver().getType(uri);
                            if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType)) {
                                readDocxAndDisplay(uri); // new method for DOCX
                            } else if ("text/plain".equals(mimeType)) {
                                readTextFromUri(uri); // already existing
                            } else {
                                Toast.makeText(this, "Unsupported file format", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );



        /* this block of code tells andoid that "Hey Android, open the document picker so
        the user can select a .txt file. But only show files that can be opened and are of text/plain type.
         */
        ImageButton fileButton = findViewById(R.id.pdfFromFile);
        fileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // mo open sa system file picker
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE); // only files that can be opened ang ipakita for privacy
            String[] mimeTypes = {"text/plain", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            filePicker.launch(intent);

        });




            // Register the PDF creation intent launcher
            createPdfLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            writePdfToUri(uri);
                        }
                    }
            );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

// file picker variable para maka kuha ug file ang user
    private ActivityResultLauncher <Intent> filePicker;

    private void readDocxAndDisplay(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            XWPFDocument document = new XWPFDocument(inputStream);

            StringBuilder builder = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                builder.append(paragraph.getText()).append("\n");
            }

            editText.setText(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read DOCX file", Toast.LENGTH_SHORT).show();
        }
    }




    private  void readTextFromUri(Uri uri){
        try{
            InputStream inputStream =  getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            reader.close();
            inputStream.close();

            EditText editText = findViewById(R.id.editTextPdfInput);
            editText.setText(stringBuilder.toString());

        }catch (IOException exception){
            exception.printStackTrace();
            Toast.makeText(this,"I failed to read the file",Toast.LENGTH_SHORT).show();
        }
    }

    /* so how does this fucntion work?
    getContentResolver().openInputStream(uri) opens a stream to the file using the special Android system

    BufferedReader reads it line-by-line for efficiency

    StringBuilder stores all those lines as a single block of text

    We call editText.setText(...) to display that text in the input area (katung asa maka type)
     */









    // for manual conversion (wala ni choose ug file ag user);
    // when the user taps the import button, kani i call
    public void exportToPdf(View v) {
        EditText filenameEditText = findViewById(R.id.editTextFilename);
        String filename = filenameEditText.getText().toString().trim();

        if (editText.getText().toString().isEmpty()) {
            Toast.makeText(this, "No text to export!", Toast.LENGTH_SHORT).show();
            return;
        }

        //ensures nga naay file name
        if (filename.isEmpty()) {
            Toast.makeText(this, "Please enter a filename", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure .pdf extension
        if (!filename.endsWith(".pdf")) {
            filename += ".pdf";
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        createPdfLauncher.launch(intent);
    }


    // Writes PDF to the given Uri from SAF
    private void writePdfToUri(Uri uri) {
        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size para di na kapoy
            PdfDocument.Page page = document.startPage(pageInfo);


            // we created a new canvas (paper) and paint(ballpen)
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(14);

            int x = 40, y = 50; //starting postion nila sa papel
            for (String line : editText.getText().toString().split("\n")) {
                canvas.drawText(line, x, y, paint);
                y += paint.descent() - paint.ascent() + 8; // space between lines, baw mano na yellow warning  na
            }

            document.finishPage(page);

            // Once naa na ang  file location or the Uri, this code saves your canvas-drawn document:
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            document.writeTo(outputStream);
            document.close();
            outputStream.close();

            Toast.makeText(this, "PDF saved to Documents", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }
    }
}


// ni follow rakong chat gpt hehe
// to be documented, and studied