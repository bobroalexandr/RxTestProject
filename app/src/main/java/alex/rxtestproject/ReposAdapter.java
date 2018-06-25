package alex.rxtestproject;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ReposAdapter extends RecyclerView.Adapter<ReposAdapter.ViewHolder> {
    private List<Repository> repositories;

    public ReposAdapter(List<Repository> repositories) {
        this.repositories = repositories;
    }

    @Override
    public ReposAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.repo_item, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(repositories.get(position).name);
    }

    @Override
    public int getItemCount() {
        return repositories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

}