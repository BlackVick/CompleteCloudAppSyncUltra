package com.cloudappsync.ultra.Utilities;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import android.widget.Toast;
import com.cloudappsync.ultra.Models.Domains;
import com.opencsv.CSVReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

public class Methods {

    //get time of file in ftp server
    @SuppressLint("SimpleDateFormat")
    public static String getTimeFromCalender(Context ctx, Calendar cal){

        Date theDate = cal.getTime();
        SimpleDateFormat time_format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        return time_format.format(theDate);

    }

    //save new custom domains
    public static void setCustomDomains(){

        //build string
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        //check if file exist in directory and populate spinner
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_CONFIG_FOLDER);
        if (dir.exists()) {

            //set file to use
            File newFile = new File(dir.getAbsolutePath(), Common.DEFAULT_DOMAIN_FILE);
            final List<Domains> domainDataList = new ArrayList<>();

            //read csv
            try {
                CSVReader reader = new CSVReader(new FileReader(newFile));
                String[] nextLine;
                int count = 0;

                //read off first
                reader.readNext();

                while ((nextLine = reader.readNext()) != null) {

                    //populate spinner and background data
                    domainDataList.add(new Domains(nextLine[0], nextLine[1], nextLine[2], nextLine[3], nextLine[4]));

                    // nextLine[] is an array of values from the line
                    count++;
                }

            } catch (IOException e) {
                Log.d("CSV Error", e.getMessage());
            } finally {

                //save list
                Paper.book().write(Common.MASTER_DOMAINS, domainDataList);

                //delete default domain
                newFile.delete();
            }

        }

    }

    //get current domain name
    public static String getCurrentDomainName(String theMaster){

        List<Domains> domainList = Paper.book().read(Common.MASTER_DOMAINS);

        //init
        String theName = null;

        //loop
        for (int k = 1; k < domainList.size(); k++){

            if (domainList.get(k).getWeb_domain().equals(theMaster))
                theName = domainList.get(k).getName();

        }

        return theName;

    }

    //get current domain data
    public static Domains getCurrentDomainData(String theMaster){

        List<Domains> domainList = Paper.book().read(Common.MASTER_DOMAINS);

        //init
        Domains theData = null;

        //loop
        for (int k = 1; k < domainList.size(); k++){

            if (domainList.get(k).getWeb_domain().equals(theMaster))
                theData = domainList.get(k);

        }

        return theData;

    }

    //get parsed item list
    public static List<String> getParsedItems(){

        //init list
        List<String> theList = new ArrayList<>();

        //get licence
        String baseUrl = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);
        String company = Paper.book().read(Common.COMPANY_ID);
        String licence = Paper.book().read(Common.LICENCE_ID);

        //page url
        String pageUrl = baseUrl + "/" + company + "/" + licence + "/App/Application/index.html";

        new Thread(() -> {
            try {

                Document doc = Jsoup.connect(pageUrl).get();

                //loop through all links
                Elements links = doc.select("link[href]");
                for (Element link: links){

                    if (!link.attr("href").startsWith("#") && !link.attr("href").startsWith("http")) {
                        theList.add(link.attr("href"));
                    }

                }

                //loop through all srcs
                Elements srcs = doc.select("[src]");
                for (Element src: srcs){

                    if (!src.attr("src").startsWith("#") && !src.attr("src").startsWith("http")) {
                        theList.add(src.attr("src"));
                    }

                }

                //loop through all frames
                Elements frames = doc.select("frame[src]");
                for (Element frame: frames){

                    if (!frame.attr("src").startsWith("#") && !frame.attr("src").startsWith("http")) {
                        theList.add(frame.attr("abs:src"));
                    }

                }

                //loop through all iframes
                Elements iFrames = doc.select("iframe[src]");
                for (Element iFrame: iFrames){

                    if (!iFrame.attr("src").startsWith("#") && !iFrame.attr("src").startsWith("http")) {
                        theList.add(iFrames.attr("src"));
                    }

                }

                //loop through all backgrounds
                Elements backgrounds = doc.select("[background]");
                for (Element background: backgrounds){

                    if (!background.attr("src").startsWith("#") && !background.attr("src").startsWith("http")) {
                        theList.add(background.attr("src"));
                    }

                }

                //loop through all styles
                Elements styles = doc.select("[style]");
                for (Element style: styles){

                    if (!style.attr("src").startsWith("#") && !style.attr("src").startsWith("http")) {
                        theList.add(style.attr("style"));
                    }

                }

                //loop through all scripts
                Elements scripts = doc.select("script[src]");
                for (Element script: scripts){

                    if (!script.attr("src").startsWith("#") && !script.attr("src").startsWith("http")) {
                        theList.add(script.attr("src"));
                    }

                }

                //loop through all images
                Elements images = doc.select("img[src]");
                for (Element image: images){

                    if (!image.attr("src").startsWith("#") && !image.attr("src").startsWith("http")) {
                        theList.add(image.attr("src"));
                    }

                }

                //loop through all complex images
                Elements complexImages = doc.select("img[data-canonical-src]");
                for (Element complexImage: complexImages){

                    if (!complexImage.attr("src").startsWith("#") && !complexImage.attr("src").startsWith("http")) {
                        theList.add(complexImage.attr("data-canonical-src"));
                    }

                }

                //loop through all videos
                Elements videos = doc.select("video:not([src])");
                for (Element video: videos){

                    if (!video.attr("src").startsWith("#") && !video.attr("src").startsWith("http")) {
                        theList.add(video.attr("src"));
                    }

                }

                //loop through all videos alt
                Elements videosAlt = doc.select("video([src])");
                for (Element video: videosAlt){

                    if (!video.attr("src").startsWith("#") && !video.attr("src").startsWith("http")) {
                        theList.add(video.attr("src"));
                    }

                }

                //loop through all videos
                Elements as = doc.select("a[href]");
                for (Element a: as){

                    if (!a.attr("src").startsWith("#") && !a.attr("src").startsWith("http")) {
                        theList.add(a.attr("href"));
                    }

                }





                Log.d("Parsed", "Parse Links" + theList.toString() + "  The length: " + theList.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return theList;
    }

    //check internet
    public static boolean isConnected(Context context)
    {
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Service.CONNECTIVITY_SERVICE);

        if (connectivityManager!=null)
        {
            NetworkInfo info=connectivityManager.getActiveNetworkInfo();
            if (info!=null)
            {
                if (info.getState()== NetworkInfo.State.CONNECTED)
                {
                    return true;
                }
            }
        }
        return false;
    }

}
