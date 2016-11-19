package ca.com.maspubsubsample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ca.mas.messaging.MASMessage;

import java.util.ArrayList;


public class TopicMessagesAdapter extends RecyclerView.Adapter<TopicMessagesAdapter.ViewHolder> {
    private ArrayList<MASMessage> mDataset;
    private Context context;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        TextView textViewQos;

        public ViewHolder(View v) {
            super(v);
            textViewMessage = (TextView) v.findViewById(R.id.recycler_message_item_text_view_message);
            textViewQos = (TextView) v.findViewById(R.id.recycler_message_item_text_view_qos);
        }
    }

    public TopicMessagesAdapter(ArrayList<MASMessage> mDataset, Context context) {
        this.mDataset = mDataset;
        this.context = context;
    }

    @Override
    public TopicMessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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

        holder.textViewMessage.setText(m);
        holder.textViewQos.setText(String.format(context.getResources().getString(R.string.recycler_message_item_qos), qos));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

