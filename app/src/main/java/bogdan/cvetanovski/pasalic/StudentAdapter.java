package bogdan.cvetanovski.pasalic;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;

/*
 * StudentAdapter acts as a bridge between the data set and the
 * UI displaying it. Currently the adapter pulls data from a
 * test StudentModel class that doesn't store any persistent
 * data, meaning that the model resets itself every time the
 * admin activity is unloaded.
 */
public class StudentAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    protected StudentModel model;
    @Override
    public int getCount() {
        return model.size();
    }

    @Override
    public Object getItem(int position) {
        return model.getItem(position);
    }

    public Object getItem(String index) {
        return model.getItem(index);
    }

    @Override
    public long getItemId(int position) {
        return model.getItem(position).getIndex().hashCode();
    }

    public void removeItem(String index) {
        model.removeItem(index);
        // Update the adapter as the underlying data model is changed
        notifyDataSetChanged();
    }

    // LayoutInflater is required to instantiate the ListView's delegate items
    public StudentAdapter(LayoutInflater inflater) {
        layoutInflater = inflater;
        model = new StudentModel();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Create a new delegate item for every item in the model
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.student_delegate, parent, false);
        }
        ImageView studentPhoto = convertView.findViewById(R.id.studentPhoto);
        TextView name = convertView.findViewById(R.id.name);
        TextView index = convertView.findViewById(R.id.index);
        CheckBox selected = convertView.findViewById(R.id.selection);

        int imgRes = model.getItem(position).getImage();
        if(imgRes != -1) {
            studentPhoto.setImageResource(imgRes);
        } else {
            // Use default image if nothing is provided. Also fixes issue with recycling delegate items
            studentPhoto.setImageResource(R.drawable.baseline_person_48);
        }

        String nameStr = model.getItem(position).getFirstName() + " " + model.getItem(position).getLastName();
        name.setText(nameStr);
        index.setText(model.getItem(position).getIndex());

        selected.setOnClickListener(v -> {
            // Use AlertDialog Builder to set up an alert dialog for deletion
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            String title = v.getContext().getResources().getString(R.string.DeleteConfirmationTitle);
            String text = v.getContext().getResources().getString(R.string.DeleteConfirmationText);
            String stringYes = v.getContext().getResources().getString(R.string.VoteYes);
            String stringNo = v.getContext().getResources().getString(R.string.VoteNo);
            builder.setTitle(title);
            builder.setMessage(text);
            builder.setCancelable(true);
            builder.setPositiveButton(stringYes, (dialog, which) -> {
                removeItem(model.getItem(position).getIndex());
                selected.setChecked(false);
            });
            builder.setNegativeButton(stringNo, (dialog, which) -> {
                dialog.cancel();
                selected.setChecked(false);
            });
            builder.setOnCancelListener(dialog -> {
                selected.setChecked(false);
            });
            AlertDialog d = builder.create();
            d.show();
        });

        return convertView;
    }
}
