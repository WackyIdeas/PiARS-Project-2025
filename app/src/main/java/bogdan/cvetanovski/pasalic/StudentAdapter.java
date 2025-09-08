package bogdan.cvetanovski.pasalic;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Toast;

/*
 * StudentAdapter acts as a bridge between the data set and the
 * UI displaying it. Currently the adapter pulls data from a
 * test StudentModel class that doesn't store any persistent
 * data, meaning that the model resets itself every time the
 * admin activity is unloaded.
 */
public class StudentAdapter extends BaseAdapter {

    LayoutInflater layoutInflater;
    Context context;
    //protected StudentModel model;

    protected User[] model;
    @Override
    public int getCount() {
        if(model == null) return 0;
        return model.length;
    }

    @Override
    public Object getItem(int position) {
        if(model == null) return null;
        if(position < 0 || position >= model.length) return null;
        return model[position];
    }

    @Override
    public long getItemId(int position) {
        if(model == null) return -1;
        if(position < 0 || position >= model.length) return -1;
        return model[position].getId();
    }

    public void removeItem(int index) {
        String msg = DatabaseManager.getInstance(context).removeItem(DatabaseManager.USERS_TABLE, DatabaseManager.UserID, index);
        if(!msg.isEmpty()) Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        populateModel();
        // Update the adapter as the underlying data model is changed
        notifyDataSetChanged();
    }

    void populateModel() {
        try {
            model = (User[])DatabaseFactory.getQueryResults(context, DatabaseManager.USERS_TABLE);
        } catch (InvalidTableException e) {
            throw new RuntimeException(e);
        }
    }
    // LayoutInflater is required to instantiate the ListView's delegate items
    public StudentAdapter(Context c, LayoutInflater inflater) {
        layoutInflater = inflater;
        context = c;
        // Load from database
        populateModel();
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

        studentPhoto.setImageResource(R.drawable.baseline_person_48);
        /*int imgRes = model.getItem(position).getImage();
        if(imgRes != -1) {
            studentPhoto.setImageResource(imgRes);
        } else {
            // Use default image if nothing is provided. Also fixes issue with recycling delegate items
        }*/

        String nameStr = model[position].getName() + " " + model[position].getSurname();
        name.setText(nameStr);
        index.setText(model[position].getUsername());

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
                removeItem(model[position].getId());
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
