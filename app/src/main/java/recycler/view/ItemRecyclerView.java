package recycler.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.aljaki.gtd3.R;

import java.util.ArrayList;

import Categorised.Items.Item;

/**
 * Created by Al Jaki on 2018/05/15.
 */

public class ItemRecyclerView extends RecyclerView.Adapter<ItemRecyclerView.ViewHolder> {

    //Contains the ArrayList of items
    private ArrayList<Item> itemslist;
    //listener to respond to clicks on recyclerview items
    private onItemClickListener ilistener;

    public interface onItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onCheckClick(int position);
        void onUndoClick(int position);
    }

    public void setOnItemClickListener(onItemClickListener listener){
        ilistener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private CheckBox completedItem; // checkbox for if the item was completed
        private TextView Title;         //textview for the title of the todo item
        private TextView Description;    //textview for the description of the todo item
        private ImageView delete;        ////delete image of the todo item
        private ImageView undo;         //undo image to move item back to category


        public ViewHolder(final View itemView, final onItemClickListener listener){
            super(itemView);

            //Create references to the views
            completedItem = itemView.findViewById(R.id.rvCompleted);
            Title = itemView.findViewById(R.id.rvTitle);
            Description = itemView.findViewById(R.id.rvDescription);
            delete = itemView.findViewById(R.id.rvDelete);
            undo = itemView.findViewById(R.id.undo);

            //set onclick to each item in the recyclerview
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(listener != null){
                        //get position of the item that is clicked
                        int position = getAdapterPosition();
                        //Enusre position is valid
                        if(position != RecyclerView.NO_POSITION){
                            //pass postion to inteface method
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            //onclick listener to undo the deleted item
            undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(listener != null){
                        //get position of the item that is clicked
                        int position = getAdapterPosition();
                        //Enusre position is valid
                        if(position != RecyclerView.NO_POSITION){
                            //pass postion to inteface method
                            listener.onUndoClick(position);
                        }
                    }
                }
            });

            //onclick listener to delete the item
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(listener != null){
                        //get position of the item that is clicked
                        int position = getAdapterPosition();
                        //Enusre position is valid
                        if(position != RecyclerView.NO_POSITION){
                            //pass postion to inteface method
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            //set onclick listener to check if the item has been completed
            completedItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(listener != null){
                        //get position of the item that is clicked
                        int position = getAdapterPosition();
                        //Enusre position is valid
                        if(position != RecyclerView.NO_POSITION){
                            //pass postion to inteface method
                            listener.onCheckClick(position);
                        }
                    }
                }
            });
        }
    }

    public ItemRecyclerView(ArrayList<Item> items){
        itemslist = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //passing the card layout to the adapter
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.basket_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v, ilistener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //acquiring todo items information and passing it to the adapter
        Item currentitem = itemslist.get(position);

        //disable checkbox if the item is logged and display undo image if item is logged
        if(currentitem.isLogged()){
            holder.completedItem.setChecked(true);
            holder.completedItem.setEnabled(false);
        }else
        {
            holder.undo.setVisibility(View.GONE);
            holder.completedItem.setChecked(currentitem.isCompleted());
        }
        holder.Title.setText(currentitem.getTitle());
        holder.Description.setText(currentitem.getDescription());
    }

    @Override
    public int getItemCount() {

        //define how many items there are in the list
        return itemslist.size();
    }
}
