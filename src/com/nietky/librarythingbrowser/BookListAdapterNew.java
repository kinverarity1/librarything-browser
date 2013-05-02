package com.nietky.librarythingbrowser;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BookListAdapterNew extends BaseAdapter {
    List<Book> books;
    String[] fieldNames;
    boolean showAdvancedSearch = false;
    
    LayoutInflater inflater;
    
    public final static int VIEW_TYPE_ADVANCED_SEARCH = 0;
    public final static int VIEW_TYPE_HEADER = 1;
    public final static int VIEW_TYPE_BOOK = 2;
    
    public final static int VIEW_TYPES = 3; 
    
    public BookListAdapterNew (Context context, List<Book> books, boolean showAdvancedSearch) {
        inflater = LayoutInflater.from(context);
        this.books = books;
        this.showAdvancedSearch = showAdvancedSearch;
    }
    
    public int getCount() {
        return books.size();
    }
    
    public int getViewTypeCount () {
        return VIEW_TYPES;
    }
    
    public int getItemViewType (int position) {
        int viewType = -1;
        if (showAdvancedSearch)
            position -= 1;
        if (position == -1)
            viewType = VIEW_TYPE_ADVANCED_SEARCH;
        else if (position == 0)
            viewType = VIEW_TYPE_HEADER;
        else
            viewType = VIEW_TYPE_BOOK;
        return viewType;
    }
    
    public int getBookListPosition (int position) {
        if (showAdvancedSearch)
            position -= 1;
        return position - 1;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);
        if (v == null) {
            holder = new ViewHolder();
            switch (type) {
            case VIEW_TYPE_ADVANCED_SEARCH:
                v = inflater.inflate(R.layout.book_list_click_through, null);
                holder.title = (TextView) v.findViewById(R.id.book_list_click_through_text);
                break;
            case VIEW_TYPE_HEADER:
                v = inflater.inflate(R.layout.book_list_result_detail, null);
                holder.title = (TextView) v.findViewById(R.id.book_list_result_detail_text);
                break;
            case VIEW_TYPE_BOOK:
                v = inflater.inflate(R.layout.book_list_item, null);
                holder.title = (TextView) v.findViewById(R.id.book_list_item_title);
                holder.subtitle = (TextView) v.findViewById(R.id.book_list_item_subtitle);
                break;
            }
            v.setTag(holder);
        } else 
            holder = (ViewHolder) v.getTag();
        switch (type) {
            case VIEW_TYPE_ADVANCED_SEARCH:
                holder.title.setText("Advanced search...");
                break;
            case VIEW_TYPE_HEADER:
                holder.title.setText(getCount() + " results:");
                break;
            case VIEW_TYPE_BOOK:
                Book book = books.get(getBookListPosition(position));
                holder.title.setText(FormatText.asHtml(book.title));
                holder.subtitle.setText(FormatText.asHtml(book.authorFirstLast));
                break;
        }
        return v;
    }
    
    static class ViewHolder {
        View view;
        TextView textview;
        TextView title;
        TextView subtitle;
        int position;
    }

}
