package ca.com.maspubsubsample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ca.mas.messaging.MASMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private ArrayList<MASMessage> mDataset;
    private Context context;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        TextView textViewQos;
        TextView textViewTopic;
        TextView textViewTimeReceived;

        public ViewHolder(View v) {
            super(v);
            textViewMessage = (TextView) v.findViewById(R.id.recycler_message_item_text_view_message);
            textViewQos = (TextView) v.findViewById(R.id.recycler_message_item_text_view_qos);
            textViewTopic = (TextView) v.findViewById(R.id.recycler_message_item_text_view_topic);
            textViewTimeReceived = (TextView) v.findViewById(R.id.recycler_message_item_text_view_time_received);
        }
    }

    public MessagesAdapter(Context context) {
        mDataset = new ArrayList<>();
        this.context = context;
    }

    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_message_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MASMessage message = mDataset.get(position);
        byte[] msg = message.getPayload();
        String m = new String(msg);
        String topic = message.getTopic();
        Integer qos = message.getQos();
        Date date = new Date(message.getSentTime());

        holder.textViewMessage.setText(m);
        holder.textViewTopic.setText(String.format(context.getResources().getString(R.string.recycler_message_item_topic), topic));
        holder.textViewQos.setText(String.format(context.getResources().getString(R.string.recycler_message_item_qos), qos));
        holder.textViewTimeReceived.setText((new SimpleDateFormat("HH:mm:ss", Locale.US)).format(date));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addMessage(MASMessage masMessage){
        mDataset.add(masMessage);
        notifyItemInserted(mDataset.size()-1);
    }
}

