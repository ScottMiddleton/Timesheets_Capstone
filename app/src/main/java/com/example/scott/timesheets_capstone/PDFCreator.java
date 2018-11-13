package com.example.scott.timesheets_capstone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;

import com.example.scott.timesheets_capstone.model.Contract;
import com.example.scott.timesheets_capstone.model.Timesheet;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class PDFCreator {

    private final String APPROVE_CLOUD_FUNCTION_BASE_URL =
            "https://us-central1-timesheets-6bb48.cloudfunctions.net/approveTimesheet?";
    private final String REJECT_CLOUD_FUNCTION_BASE_URL =
            "https://us-central1-timesheets-6bb48.cloudfunctions.net/rejectTimesheet?";
    private final String FIREBASE_STORAGE_BASE_URL =
            "https://firebasestorage.googleapis.com/v0/b/timesheets-6bb48.appspot.com/o/timesheet_template.pdf?alt=media&token=f5db231a-21f2-45b7-a0ab-b8f92c44f8f8\n";

    private FirebaseStorage storage;
    private File outputFile;

    private FirebaseAuth mAuth;
    private Timesheet timesheet;
    private Contract contract;
    private ProgressBar loadingIndicator;

    private Context mContext;

    public PDFCreator(Context context, Timesheet timesheet, Contract contract, ProgressBar loadingIndicator) {
        this.mContext = context;
        this.timesheet = timesheet;
        this.contract = contract;
        this.loadingIndicator = loadingIndicator;
    }

    public void generatePdf() throws IOException {
        storage = FirebaseStorage.getInstance();
        StorageReference pdfReference = storage.getReferenceFromUrl(FIREBASE_STORAGE_BASE_URL);

        final File localFile = File.createTempFile("timesheet_template", ".pdf");

        pdfReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                try {
                    stampPDF(localFile);
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void stampPDF(File localFile) throws IOException, DocumentException {

        PdfReader reader = new PdfReader(localFile.getPath());

        String fileName = "timesheet_stamped";
        String fileExtension = ".pdf";

        outputFile = File.createTempFile(fileName, fileExtension, mContext.getExternalCacheDir());
        // Create the stamper from the incoming file.
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputFile));

        mAuth = FirebaseAuth.getInstance();

        stamper.getAcroFields().setField("contractorName", Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
        stamper.getAcroFields().setField("companyName", contract.getCompanyName());
        stamper.getAcroFields().setField("weekCommencing", formatDateToString(timesheet.getWeekStartDate()));
        stamper.getAcroFields().setField("mondayHrs", formatdecimal(timesheet.getMondayHrs()));
        stamper.getAcroFields().setField("tuesdayHrs", formatdecimal(timesheet.getTuesdayHrs()));
        stamper.getAcroFields().setField("wednesdayHrs", formatdecimal(timesheet.getWednesdayHrs()));
        stamper.getAcroFields().setField("thursdayHrs", formatdecimal(timesheet.getThursdayHrs()));
        stamper.getAcroFields().setField("fridayHrs", formatdecimal(timesheet.getFridayHrs()));
        stamper.getAcroFields().setField("saturdayHrs", formatdecimal(timesheet.getSaturdayHrs()));
        stamper.getAcroFields().setField("sundayHrs", formatdecimal(timesheet.getSundayHrs()));
        stamper.getAcroFields().setField("totalHrs", getTotalHours(timesheet));

        PdfContentByte canvas = stamper.getOverContent(1);

        Bitmap approveBm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.approve_button);
        Image approveImage = getImageFromBitmap(approveBm);

        Bitmap rejectBm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.reject_button);
        Image rejectImage = getImageFromBitmap(rejectBm);

        Chunk approveChunk = new Chunk(approveImage, -10, 65);

        Chunk rejectChunk = new Chunk(rejectImage, 10, 65);

        approveChunk.setAnchor(generateApprovalUrl());
        rejectChunk.setAnchor(generateRejectUrl());

        Phrase p = new Phrase();
        p.add(approveChunk);
        Phrase p2 = new Phrase();
        p.add(rejectChunk);
        ColumnText ct = new ColumnText(canvas);
        ct.setSimpleColumn(225, 250, 450, 100);
        ct.addText(p);
        ct.addText(p2);
        ct.go();

        stamper.close();
        reader.close();
        storage.getReference().child("Stamped").child(contract.getCompanyName()).child(timesheet.getId()).putFile(Uri.fromFile(outputFile));
        sendTimesheetEmail();
    }

    private void sendTimesheetEmail() {
        Uri uri = Uri.fromFile(outputFile);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", contract.getApprovingManagerEmail(), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getEmailSubject());
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                getEmailBody());
        mContext.startActivity(Intent.createChooser(emailIntent, "Send email..."));
        loadingIndicator.setVisibility(View.GONE);
    }

    private String getTotalHours(Timesheet timesheet) {
        double totalHours = (timesheet.getMondayHrs() +
                timesheet.getTuesdayHrs() +
                timesheet.getWednesdayHrs() +
                timesheet.getThursdayHrs() +
                timesheet.getFridayHrs() +
                timesheet.getSaturdayHrs() +
                timesheet.getSundayHrs());
        return formatdecimal(totalHours);
    }

    private String formatdecimal(double totalHours) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        return format.format(totalHours);
    }

    private String getEmailSubject() {
        return Objects
                .requireNonNull(mAuth.getCurrentUser())
                .getDisplayName() + mContext.getString(R.string.email_subject_append)
                + formatDateToString(timesheet.getWeekStartDate());
    }

    private Image getImageFromBitmap(Bitmap bm) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image i = null;
        byte[] byteArray = stream.toByteArray();
        try {
            i = Image.getInstance(byteArray);
            i.scaleAbsoluteHeight(37);
            i.scaleAbsoluteWidth(65);
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }
        return i;
    }

    private String generateApprovalUrl() {
        return APPROVE_CLOUD_FUNCTION_BASE_URL +
                mContext.getString(R.string.cloud_user_append) + Objects.requireNonNull(mAuth.getCurrentUser()).getUid() +
                mContext.getString(R.string.cloud_company_append) + formatCompanyName(contract.getCompanyName()) +
                mContext.getString(R.string.cloud_timesheet_append) + timesheet.getId();
    }

    private String generateRejectUrl() {
        return REJECT_CLOUD_FUNCTION_BASE_URL +
                mContext.getString(R.string.cloud_user_append) + Objects.requireNonNull(mAuth.getCurrentUser()).getUid() +
                mContext.getString(R.string.cloud_company_append) + formatCompanyName(contract.getCompanyName()) +
                mContext.getString(R.string.cloud_timesheet_append) + timesheet.getId();
    }

    private String formatCompanyName(String company) {
        return company.replaceAll("\\s", "+");
    }

    private String getEmailBody() {
        return (contract.getApprovingManagerName() + ",\n\n") +
                Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName() + " " +
                mContext.getString(R.string.email_body_append_week_commencing) + " " +
                formatDateToString(timesheet.getWeekStartDate()) + ".\n" +
                mContext.getString(R.string.email_body_append_see_attached);
    }

    private String formatDateToString(long milliSeconds) {
        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat(mContext.getString(R.string.date_format));
        Date date = new Date(milliSeconds);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(date);
    }
}

