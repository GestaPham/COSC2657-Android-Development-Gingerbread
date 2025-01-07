package com.gingerbread.asm3.Views.Support;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.gingerbread.asm3.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HelpCenterActivity extends AppCompatActivity {

    private TextView tabFAQ, tabContactUs;
    private LinearLayout faqContainer, contactUsContainer;
    private EditText searchBar;

    private List<FAQItem> faqList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        tabFAQ = findViewById(R.id.tabFAQ);
        tabContactUs = findViewById(R.id.tabContactUs);
        faqContainer = findViewById(R.id.faqContainer);
        contactUsContainer = findViewById(R.id.contactUsContainer);
        searchBar = findViewById(R.id.searchBar);

        activateTab(tabFAQ);
        deactivateTab(tabContactUs);

        faqList = loadFAQItems();
        displayFAQs(faqList);

        buttonBack.setOnClickListener(v -> finish());

        tabFAQ.setOnClickListener(v -> {
            activateTab(tabFAQ);
            deactivateTab(tabContactUs);
            faqContainer.setVisibility(View.VISIBLE);
            contactUsContainer.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
        });

        tabContactUs.setOnClickListener(v -> {
            activateTab(tabContactUs);
            deactivateTab(tabFAQ);
            faqContainer.setVisibility(View.GONE);
            contactUsContainer.setVisibility(View.VISIBLE);
            searchBar.setVisibility(View.GONE);
        });

        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFAQs(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private List<FAQItem> loadFAQItems() {
        List<FAQItem> faqItems = new ArrayList<>();
        try {
            InputStream is = getAssets().open("faq_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray faqArray = new JSONArray(json);
            for (int i = 0; i < faqArray.length(); i++) {
                JSONObject faqObject = faqArray.getJSONObject(i);
                String question = faqObject.getString("question");
                String answer = faqObject.getString("answer");

                faqItems.add(new FAQItem(question, answer));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return faqItems;
    }

    private void filterFAQs(String query) {
        List<FAQItem> filteredList = new ArrayList<>();
        for (FAQItem faq : faqList) {
            if (faq.getQuestion().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(faq);
            }
        }
        displayFAQs(filteredList);
    }

    private void displayFAQs(List<FAQItem> faqs) {
        faqContainer.removeAllViews();
        for (FAQItem faq : faqs) {
            View faqView = createFAQView(faq);
            faqContainer.addView(faqView);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) faqView.getLayoutParams();
            params.setMargins(0, 0, 0, 16);
            faqView.setLayoutParams(params);
        }
    }

    private View createFAQView(FAQItem faq) {
        View view = getLayoutInflater().inflate(R.layout.item_faq, null);

        TextView textViewQuestion = view.findViewById(R.id.textViewQuestion);
        TextView textViewAnswer = view.findViewById(R.id.textViewAnswer);

        textViewQuestion.setText(faq.getQuestion());
        textViewAnswer.setText(faq.getAnswer());
        textViewAnswer.setVisibility(View.GONE);

        view.setOnClickListener(v -> {
            textViewAnswer.setVisibility(
                    textViewAnswer.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
        });

        return view;
    }

    private void activateTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.tab_active_bg);
        tab.setTextColor(getResources().getColor(R.color.text_dark));
    }

    private void deactivateTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.tab_inactive_bg);
        tab.setTextColor(getResources().getColor(R.color.text_dark));
    }
}

class FAQItem {
    private String question;
    private String answer;

    public FAQItem(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}
