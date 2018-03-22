package dkt01.speedscout18;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.ArrayList;

public class MatchListAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<Pair<Integer, String> > kvPair;
    private ArrayList<String> mIdList;
    private boolean checkBoxesVisible = false;

    public MatchListAdapter(Context context, ArrayList<Pair<Integer,String> > arrayList) {
        this.mContext = context;
        kvPair = new ArrayList<>();
        mIdList = new ArrayList<>();
        for (Pair<Integer,String> entry : arrayList) {
            add(entry);
        }
    }

    public MatchListAdapter(Context context, Pair<Integer,String>[] arrayList){
        this.mContext = context;
        kvPair = new ArrayList<>();
        mIdList = new ArrayList<>();
        for (Pair<Integer,String> entry : arrayList) {
            add(entry);
        }
    }

    public void Update(ArrayList<Pair<Integer,String> > newList)
    {
        mIdList.clear();
        kvPair.clear();
        for (Pair<Integer,String> entry : newList)
        {
            add(entry);
        }
    }

    public Pair<Integer,String> getObject(int position) {
        return kvPair.get(position);
    }

    public boolean add(Pair<Integer,String> object) {
        String id = object.second;

        mIdList.add(id);
        kvPair.add(object);
        this.notifyDataSetChanged();
        return true;
    }

    public void remove(int id){
        boolean removed = false;
        for(Pair<Integer,String> entry : kvPair)
        {
            if(entry.first == id)
            {
                mIdList.remove(kvPair.indexOf(entry));
                kvPair.remove(entry);
                removed = true;
                break;
            }
        }
        if(removed)
        {
            this.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unchecked")
    public Pair<Integer,String> getEntry(int position)
    {
        return kvPair.get(position);
    }

    @Override
    public int getCount() {
        return kvPair.size();
    }

    @Override
    public Object getItem(int position) {
        return kvPair.get(position);
    }

    @Override
    public long getItemId(int position) {
        return kvPair.get(position).first;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Pair<Integer,String> object = kvPair.get(position);
        if (view == null) {
            int layoutResource = R.layout.matches_list_view_item;
            view = LayoutInflater.from(mContext).inflate(layoutResource, null);
        }
        CheckedTextView tv = (CheckedTextView) view.findViewById(R.id.matches_list_view_text);
        tv.setText(object.second);
        if(checkBoxesVisible)
        {
            tv.setCheckMarkDrawable(R.drawable.abc_btn_check_material);
        }
        else
        {
            tv.setCheckMarkDrawable(null);
        }
        return view;
    }

    public void showCheckBoxes(boolean show)
    {
        checkBoxesVisible = show;
        notifyDataSetChanged();
    }
}
