package com.mycampusdock.dock;

import android.view.View;

public class DockInterfaces {

    public interface OnItemClickListener{
        void onItemClicked(Object item, View view);
    }

    public interface OnQueryCompleteListener{
        void onQueryCompleted(boolean status, int type);
    }

    public interface FragmentLifecycle {
        public void onPauseFragment();
        public void onResumeFragment();

    }
}
