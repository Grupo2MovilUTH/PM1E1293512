package com.example.pm1e1293512;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Contact> mContacts;
    private Context mContext;

    public ContactAdapter(Context context, List<Contact> contacts) {
        mContacts = contacts;
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView phoneTextView;
        public TextView noteTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contact_name);
            phoneTextView = itemView.findViewById(R.id.contact_phone);
            noteTextView = itemView.findViewById(R.id.contact_note);
        }
    }

    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.contact_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ContactAdapter.ViewHolder holder, int position) {
        Contact contact = mContacts.get(position);

        TextView nameTextView = holder.nameTextView;
        nameTextView.setText(contact.getName());

        TextView phoneTextView = holder.phoneTextView;
        phoneTextView.setText(contact.getPhone());

        TextView noteTextView = holder.noteTextView;
        noteTextView.setText(contact.getNote());
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }
}
