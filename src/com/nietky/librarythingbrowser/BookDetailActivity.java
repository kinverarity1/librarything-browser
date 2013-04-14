package com.nietky.librarythingbrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.TextView;

public class BookDetailActivity extends Activity {
    String TAG = "BookDetailActivity";
    SharedPreferences sharedPref;
    LogHandler logger;
    String id;
    Cursor cursor;
    Context context;
    HashMap<String, String> fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String METHOD = "-onCreate():";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        logger = new LogHandler(sharedPref);
        
        context = this;

        Intent intent = getIntent();
        id = intent.getStringExtra("_id");
        DbHelperNew dbHelper = new DbHelperNew(getApplicationContext());
        dbHelper.open();
        cursor = dbHelper.getRow(id);
        cursor.moveToFirst();
        Log.d(TAG + METHOD, "Loading _id=" + id);

        fields = new HashMap<String, String>();

        String fieldname;
        String[] fieldnames = { "_id", "book_id", "title", "author1",
                "author2", "author_other", "publication", "date", "ISBNs",
                "series", "source", "lang1", "lang2", "lang_orig", "LCC",
                "DDC", "bookcrossing", "date_entered", "date_acquired",
                "date_started", "date_ended", "stars", "collections", "tags",
                "review", "summary", "comments", "comments_private", "copies",
                "encoding" };
        for (int i = 0; i < fieldnames.length; i += 1) {
            fieldname = fieldnames[i];
//            Log.d(TAG, "Getting content for fieldname=" + fieldname);
            int index = cursor.getColumnIndex(fieldname);
            String content = "";
            if (index > -1)
                content = cursor.getString(index);
            content = content.replace("[return]", "\n");
            content = content.trim();
            fields.put(fieldname, content);
        }
        dbHelper.close();

        setTitle(fields.get("title"));
        TextView titleView = (TextView) findViewById(R.id.book_detail_title);
        titleView.setText(FormatText.asHtml(fields.get("title")));
        String ISBN = fields.get("ISBNs").replace('[', ' ').replace(']', ' ');
        String title = fields.get("title").trim().toLowerCase()
                .replace(' ', '_');
        ISBN = ISBN.trim();
        String URL = "";
        if (ISBN.length() > 0) {
            URL = "http://www.librarything.com/isbn/" + ISBN;
        } else {
            URL = "http://www.librarything.com/title/" + title;
        }
        final String finalURL = URL;
        titleView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(finalURL));
                startActivity(intent);
            }
        });

        TextView authorView = (TextView) findViewById(R.id.book_detail_author);
        String author;
        String author1 = fields.get("author1");
        String author2 = fields.get("author2");
        if (author2 == "") {
            author = author1;
        } else {
            author = author2;
        }
        if (fields.get("author_other").length() > 0) {
            author += ", " + fields.get("author_other");
        }
        authorView.setText(FormatText.asHtml(author));
        authorView.setTag(author);
        authorView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TextView self = (TextView) v;
                String author = (String) self.getTag();
                Intent intent = new Intent(context, BookListActivity.class);
                intent.putExtra("authorName", author);
//                // The following MUST be made subject to a preference for exclusive/inclusive/ask tag/collection handling
//                intent.putExtra("ids", getIds());
                startActivity(intent);
            }
        });

        TextView publication = (TextView) findViewById(R.id.book_detail_publication);
        String publicationDetails = fields.get("publication");
        if (publicationDetails.length() == 0) {
            publicationDetails = getString(R.string.details_ui_publication_preface) + fields.get("date");
        }
        if (ISBN.length() > 0) {
            if (publicationDetails.length() > 0) {
                publicationDetails += "; ";
            }
            publicationDetails += "ISBN: " + ISBN + ".";
        }
        publication.setText(FormatText.asHtml(publicationDetails));

        ArrayList<String> tagNames = new ArrayList<String>();
        Collections.addAll(tagNames, fields.get("tags").split(","));
        addLabels((LinearLayout) findViewById(R.id.book_detail_tags_container),
                getString(R.string.details_ui_tags_preface), tagNames, "tagName", R.drawable.rounded_edges_tag);

        ArrayList<String> collectionNames = new ArrayList<String>();
        Collections.addAll(collectionNames, fields.get("collections")
                .split(","));
        addLabels(
                (LinearLayout) findViewById(R.id.book_detail_collections_container),
                getString(R.string.details_ui_collections_preface), collectionNames, "collectionName",
                R.drawable.rounded_edges_collection);

        TextView dateEntered = (TextView) findViewById(R.id.book_detail_date_entered);
        if (fields.get("date_entered").length() > 0) {
            dateEntered.setText(FormatText.asHtml(getString(R.string.details_ui_date_entered_preface) + fields.get("date_entered")));
        } else {
            dateEntered.setVisibility(View.GONE);
        }
        TextView dateAcquired = (TextView) findViewById(R.id.book_detail_date_acquired);
        if (fields.get("date_acquired").length() > 0) {
            dateAcquired.setText(FormatText.asHtml(getString(R.string.details_ui_date_acquired_preface) + fields.get("date_acquired")));
        } else {
            dateAcquired.setVisibility(View.GONE);
        }
        TextView dateStarted = (TextView) findViewById(R.id.book_detail_date_started);
        if (fields.get("date_started").length() > 0) {
            dateStarted.setText(FormatText.asHtml(getString(R.string.details_ui_date_started_preface) + fields.get("date_started")));
        } else {
            dateStarted.setVisibility(View.GONE);
        }
        TextView dateEnded = (TextView) findViewById(R.id.book_detail_date_ended);
        if (fields.get("date_ended").length() > 0) {
            dateEnded.setText(FormatText.asHtml(getString(R.string.details_ui_date_ended_preface) + fields.get("date_ended")));
        } else {
            dateEnded.setVisibility(View.GONE);
        }
        
        TextView review = (TextView) findViewById(R.id.book_detail_review);
        if (fields.get("review").length() > 0) {
            review.setText(FormatText.asHtml("<p><b>" + getString(R.string.details_ui_review_preface) + "</b> " + fields.get("review")));
        } else {
            review.setVisibility(View.GONE);
        }

        RatingBar ratingBar = (RatingBar) findViewById(R.id.book_detail_rating_bar);
        if (fields.get("stars").length() > 0) {
            ratingBar.setRating(Float.parseFloat((fields.get("stars"))));
            ratingBar.setClickable(false);
        } else {
            ratingBar.setVisibility(View.GONE);
        }

        TextView comments = (TextView) findViewById(R.id.book_detail_comments);
        if (fields.get("comments").length() > 0) {
            comments.setText(FormatText.asHtml("<p><b>" + getString(R.string.details_ui_comments_preface) + "</b> "
                    + fields.get("comments")));
        } else {
            comments.setVisibility(View.GONE);
        }

        TextView commentsPrivate = (TextView) findViewById(R.id.book_detail_comments_private);
        if (fields.get("comments_private").length() > 0) {
            commentsPrivate.setText(FormatText.asHtml("<p><b>" + getString(R.string.details_ui_private_comments_preface) + "</b> "
                            + fields.get("comments_private")));
        } else {
            commentsPrivate.setVisibility(View.GONE);
        }

    }

    public void addLabels(LinearLayout ll, String prefix,
            ArrayList<String> strArr, String extraLabel, int drawable) {
        final String finalExtraLabel = extraLabel;
        Display display = getWindowManager().getDefaultDisplay();
        int maxWidth = display.getWidth() - 10;
        if (strArr.size() > 0) {
            LinearLayout llAlso = new LinearLayout(context);
            llAlso.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));
            llAlso.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvIntro = new TextView(context);
            tvIntro.setText(FormatText.asHtml(prefix));
            llAlso.addView(tvIntro);
            tvIntro.measure(0, 0);

            int widthSoFar = tvIntro.getMeasuredWidth();
            for (String str : strArr) {
                TextView tvTag = new TextView(context, null,
                        android.R.attr.textColorLink);
                tvTag.setText(FormatText.asHtml(str));
                tvTag.setTextSize(14);
                tvTag.setTextColor(Color.BLACK);
                tvTag.setBackgroundResource(drawable);
                tvTag.setTag(str);
                tvTag.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        TextView self = (TextView) v;
                        String tagTagText = (String) self.getTag();
                        // Toast.makeText(getActivity(), tagTagText, 10).show();
                        Intent resultIntent = new Intent(context,
                                BookListActivity.class);
                        resultIntent.putExtra(finalExtraLabel, tagTagText);
                        startActivity(resultIntent);
                    }
                });

                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                int left_right = 5;
                int top_bottom = 3;
                llp.setMargins(left_right, top_bottom, left_right, top_bottom);
                tvTag.setLayoutParams(llp);

                tvTag.measure(0, 0);
                widthSoFar += tvTag.getMeasuredWidth() + left_right * 2;

                if (widthSoFar >= maxWidth) {
                    ll.addView(llAlso);

                    llAlso = new LinearLayout(context);
                    llAlso.setLayoutParams(new LayoutParams(
                            LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                    llAlso.setOrientation(LinearLayout.HORIZONTAL);

                    llAlso.addView(tvTag);
                    widthSoFar = tvTag.getMeasuredWidth();
                } else {

                    llAlso.addView(tvTag);
                }
            }

            ll.addView(llAlso);
        }
    }

}
