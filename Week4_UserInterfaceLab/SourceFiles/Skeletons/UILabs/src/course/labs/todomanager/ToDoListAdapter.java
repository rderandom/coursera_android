package course.labs.todomanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import course.labs.todomanager.ToDoItem.Priority;
import course.labs.todomanager.ToDoItem.Status;

public class ToDoListAdapter extends BaseAdapter {

	private final List<ToDoItem> mItems = new ArrayList<ToDoItem>();
	private final Context mContext;

	private static final String TAG = "Lab-UserInterface";

	public ToDoListAdapter(Context context) {

		mContext = context;

	}

	// Add a ToDoItem to the adapter
	// Notify observers that the data set has changed

	public void add(ToDoItem item) {

		mItems.add(item);
		notifyDataSetChanged();

	}

	// Clears the list adapter of all items.

	public void clear() {

		mItems.clear();
		notifyDataSetChanged();

	}

	// Returns the number of ToDoItems

	@Override
	public int getCount() {

		return mItems.size();

	}

	// Retrieve the number of ToDoItems

	@Override
	public Object getItem(int pos) {

		return mItems.get(pos);

	}

	// Get the ID for the ToDoItem
	// In this case it's just the position

	@Override
	public long getItemId(int pos) {

		return pos;

	}

	// Create a View for the ToDoItem at specified position
	// Remember to check whether convertView holds an already allocated View
	// before created a new View.
	// Consider using the ViewHolder pattern to make scrolling more efficient
	// See: http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// TODO OK - Get the current ToDoItem
		final ToDoItem toDoItem = (ToDoItem) getItem(position);

		// TODO OK? - Inflate the View for this ToDoItem
		// from todo_item.xml
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout itemLayout = null;
		if(parent != null){
			itemLayout = (RelativeLayout) inflater.inflate(R.layout.todo_item, parent, false);

		} else {
			itemLayout = (RelativeLayout) inflater.inflate(R.layout.todo_item, null);

		}

		// TODO - Fill in specific ToDoItem data
		// Remember that the data that goes in this View
		// corresponds to the user interface elements defined
		// in the layout file

		// TODO - Display Title in TextView
		final TextView titleView = (TextView) itemLayout.findViewById(R.id.titleView);
		titleView.setText(toDoItem.getTitle());
		
		// TODO - Set up Status CheckBox
		final CheckBox statusView = (CheckBox) itemLayout.findViewById(R.id.statusCheckBox);
		Status status = toDoItem.getStatus();
		switch (status) {
		case DONE:
			statusView.setChecked(true);
			break;
		case NOTDONE:
			statusView.setChecked(false);
			break;
		}
		statusView.setTag(Integer.valueOf(position));
		statusView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				Log.i(TAG, "Entered onCheckedChanged()");

				// TODO - set up an OnCheckedChangeListener, which
				// is called when the user toggles the status checkbox
		        Integer pos = (Integer)buttonView.getTag();
		        ToDoItem item = mItems.get(pos);
		        Status status = item.getStatus();
		        if(status == Status.DONE){
		        	item.setStatus(Status.NOTDONE);
		        } else {
		        	item.setStatus(Status.DONE);

		        }
			}
		});
		
		

		// TODO - Display Priority in a TextView
		final TextView priorityView =  (TextView) itemLayout.findViewById(R.id.priorityView);
		String priorityText = "";
		switch (toDoItem.getPriority()) {
		case LOW:
			priorityText = "LOW";
			break;
		case  MED:
			priorityText = "MED";
		break;
		default:
			priorityText = "HIGH";
			break;
		}
		priorityView.setText(priorityText);
		
		// TODO OK - Display Time and Date.
		// Hint - use ToDoItem.FORMAT.format(toDoItem.getDate()) to get date and
		// time String
		final TextView dateView = (TextView) itemLayout.findViewById(R.id.dateView);
		String timeString = ToDoItem.FORMAT.format(toDoItem.getDate());
		dateView.setText(timeString);
		
		// Return the View you just created
		return itemLayout;

	}
}
