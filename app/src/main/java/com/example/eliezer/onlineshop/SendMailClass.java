package com.example.eliezer.onlineshop;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.service.textservice.SpellCheckerService;
import android.widget.Toast;

import java.util.Properties;

/**
 * Created by Eliezer on 05/04/2018.
 */

public class SendMailClass extends AsyncTask<Void,Void,Void> {
    String  emTo,subject,body;
    Context context;
    private SpellCheckerService.Session session;
    private ProgressDialog progressDialog;

    public SendMailClass(Context context,String emTo, String subject, String body) {
        this.context = context;
        this.emTo = emTo;
        this.subject = subject;
        this.body = body;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog.show(context,"Sending email","Please Wait...",false,false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        progressDialog.dismiss();

        Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Properties properties = new Properties();

        properties.put("mail.smtp.host","smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port","465");
        properties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth","true");
        properties.put("mail.smtp.port","465");

        return null;
    }
}
