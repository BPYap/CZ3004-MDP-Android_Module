package com.example.qunjia.mdpapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.andretietz.android.controller.DirectionView;
import com.andretietz.android.controller.InputView;
import com.example.qunjia.mdpapp.OpenGL.myRenderer;


public class GridMapFragment extends Fragment {

    public static final int MOVE_NONE = 0, MOVE_UP = 1, MOVE_DOWN = 2,
            MOVE_LEFT = 3, MOVE_RIGHT = 4;

    public static Boolean is3Dmode = false;
    private static String statusWindowTxt = "";


    public static GridMapFragment newInstance(int position) {
        GridMapFragment f = new GridMapFragment();
        Bundle b = new Bundle();
        b.putInt("position", position);
        f.setArguments(b);
        return f;
    }

    static GridMapUpdateManager mapUpdateManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grid_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initSwitchListener();
        GridMapHandler2D.createGridMap(getActivity());
        directionViewSetup(getActivity());
        initPositionToggleBtnListener();
        initAutoManualToggleBtnListener();
        init3DToggleBtnListener();
        getActivity().findViewById(R.id.updateBtn).setEnabled(false);

        //
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String msg = "MDF|C000000000000000000000000000000000000000000000000000000000000000000000000003|000000000000|N|1|1";
                mapUpdateManager.decodeMessage(getContext(), msg);
            }
        }, 200);
    }

    private void initSwitchListener() {
        Switch directionSwitch = (Switch) getActivity().findViewById(R.id.directionToggleBtn);
        directionSwitch.setOnCheckedChangeListener(new AccelerometerSwitchListener());
    }

    private void directionViewSetup(final Context context) {
        DirectionView directionView = (DirectionView) ((Activity) context).findViewById(R.id.viewDirection);
        directionView.setOnButtonListener(new InputView.InputEventListener() {
            @Override
            public void onInputEvent(View view, int buttons) {


                switch (buttons & 0xff) {
                    case DirectionView.DIRECTION_UP:
                        moveRobot(context, MOVE_UP);
                        break;
                    case DirectionView.DIRECTION_DOWN:
                        moveRobot(context,MOVE_DOWN);
                        break;
                    case DirectionView.DIRECTION_LEFT:
                        moveRobot(context, MOVE_LEFT);
                        break;
                    case DirectionView.DIRECTION_RIGHT:
                        moveRobot(context, MOVE_RIGHT);
                        break;
                }
            }
        });
    }

    private void initPositionToggleBtnListener() {
        ToggleButton toggleButton = getActivity().findViewById(R.id.positionToggleBtn);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, boolean b) {
                final Activity activity = (Activity) compoundButton.getContext();
                final ToggleButton autoManualToggleBtn = activity.findViewById(R.id.autoManualToggleBtn);
                final Switch accelerometerSwitch = activity.findViewById(R.id.directionToggleBtn);
                final Button updateBtn = activity.findViewById(R.id.updateBtn);
                final Button exploreBtn = activity.findViewById(R.id.exploreBtn);
                final Button fastestBtn = activity.findViewById(R.id.fastestBtn);
                final Button stopBtn = activity.findViewById(R.id.stopBtn);

                if (compoundButton.isChecked()) {
                    GridMapHandler2D.SetRobotDragListener(compoundButton.getContext(), true);
                    directionViewSetEnabled(activity, false);

                    autoManualToggleBtn.setEnabled(false);
                    accelerometerSwitch.setEnabled(false);
                    updateBtn.setEnabled(false);
                    exploreBtn.setEnabled(false);
                    fastestBtn.setEnabled(false);
                    stopBtn.setEnabled(false);

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(compoundButton.getContext());

                    builder1.setMessage("Select an Option");
                    builder1.setCancelable(false);

                    builder1.setPositiveButton(
                            "Start",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    GridMapHandler2D.isWaypointSelected = false;

                                    AlertDialog.Builder adb = new AlertDialog.Builder(compoundButton.getContext());
                                    CharSequence items[] = new CharSequence[] {"North", "South", "East", "West"};
                                    adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface d, int n) {
                                            GridMapHandler2D.robotStartCoordinateDirection = n;
                                        }

                                    });
                                    adb.setNegativeButton("Confirm", null);
                                    adb.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            GridMapHandler2D.SetRobotDragListener(compoundButton.getContext(), false);
                                            directionViewSetEnabled(activity, true);

                                            autoManualToggleBtn.setEnabled(true);
                                            accelerometerSwitch.setEnabled(true);
                                            updateBtn.setEnabled(true);
                                            exploreBtn.setEnabled(true);
                                            fastestBtn.setEnabled(true);
                                            stopBtn.setEnabled(true);
                                            compoundButton.toggle();
                                        }
                                    });
                                    adb.setTitle("Choose a direction");
                                    adb.setCancelable(false);
                                    adb.show();
                                }
                            });

                    builder1.setNegativeButton(
                            "Waypoint",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    GridMapHandler2D.isWaypointSelected = true;
                                }
                            });
                    builder1.setNeutralButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    autoManualToggleBtn.setEnabled(true);
                                    accelerometerSwitch.setEnabled(true);
                                    updateBtn.setEnabled(true);
                                    exploreBtn.setEnabled(true);
                                    fastestBtn.setEnabled(true);
                                    stopBtn.setEnabled(true);
                                    compoundButton.toggle();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else {
                    GridMapHandler2D.SetRobotDragListener(compoundButton.getContext(), false);
                    directionViewSetEnabled(activity, true);

                    autoManualToggleBtn.setEnabled(true);
                    accelerometerSwitch.setEnabled(true);
                    updateBtn.setEnabled(true);
                    exploreBtn.setEnabled(true);
                    fastestBtn.setEnabled(true);
                    stopBtn.setEnabled(true);
                }
            }
        });
    }

    private void initAutoManualToggleBtnListener() {
        ToggleButton toggleButton = getActivity().findViewById(R.id.autoManualToggleBtn);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Activity activity = (Activity) compoundButton.getContext();
                Button updateBtn = activity.findViewById(R.id.updateBtn);
                mapUpdateManager.toggleDisplayMode();
                if (compoundButton.isChecked()) {
                    updateBtn.setEnabled(true);
                } else {
                    updateBtn.setEnabled(false);
                }
            }
        });
    }

    private void init3DToggleBtnListener() {
        ToggleButton toggleButton = getActivity().findViewById(R.id.DToggleBtn);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ConstraintLayout constraintLayout2D = getActivity().findViewById(R.id.constraintLayout2D);
                ConstraintLayout constraintLayout3D = getActivity().findViewById(R.id.constraintLayout3D);
                ToggleButton posToggleBtn = getActivity().findViewById(R.id.positionToggleBtn);

                if (compoundButton.isChecked()) {
                    is3Dmode = true;
                    GridMapHandler2D.setRobotPosition(getContext(), -1,-1);//hide robot
                    constraintLayout2D.setVisibility(View.GONE);
                    constraintLayout3D.setVisibility(View.VISIBLE);
                    addTextToStatusWindow(getActivity(), null);
                    posToggleBtn.setEnabled(false);


                } else {
                    is3Dmode = false;
                    GridMapHandler2D.setRobotPosition(getContext(), 100,100);//show robot
                    constraintLayout2D.setVisibility(View.VISIBLE);
                    constraintLayout3D.setVisibility(View.GONE);
                    addTextToStatusWindow(getActivity(), null);
                    posToggleBtn.setEnabled(true);
                }
            }
        });
    }


    public static void directionViewSetEnabled(Activity activity, Boolean enabled) {
        DirectionView directionView = activity.findViewById(R.id.viewDirection);
        DirectionView directionViewDisabled = activity.findViewById(R.id.viewDirectionDisabled);
        View directionViewDisabledBlocker = activity.findViewById(R.id.viewDirectionDisabledBlocker);
        if (enabled) {
            directionView.setVisibility(View.VISIBLE);
            directionViewDisabled.setVisibility(View.GONE);
            directionViewDisabledBlocker.setVisibility(View.GONE);
        } else {
            directionView.setVisibility(View.GONE);
            directionViewDisabled.setVisibility(View.VISIBLE);
            directionViewDisabledBlocker.setVisibility(View.VISIBLE);
        }
    }

    public static void myClickMethod(final View v) {
        switch (v.getId()) {
            case R.id.clearStatusWindowBtn2D:
            case R.id.clearStatusWindowBtn3D:
                statusWindowTxt = "";
                TextView statusWindowTV2D = ((Activity) v.getContext()).findViewById(R.id.statusWindowTV2D);
                statusWindowTV2D.setText("");
                TextView statusWindowTV3D = ((Activity) v.getContext()).findViewById(R.id.statusWindowTV3D);
                statusWindowTV3D.setText("");
                return;
            case R.id.reconfigureBtn:
                ReconfigureHandler.reconfigBtnOnClick(v.getContext());
                return;
            case R.id.F1_btn:
                ReconfigureHandler.F1BtnOnCLick(v.getContext());
                return;
            case R.id.F2_btn:
                ReconfigureHandler.F2BtnOnCLick(v.getContext());
                return;
            case R.id.stopBtn:

                return;
            case R.id.updateBtn:
                mapUpdateManager.updateAll(v.getContext());
                return;
            case R.id.fastestBtn:
                //GridMapHandler2D.setRobotPosition(v.getContext(), 5, 5);
                return;
            case R.id.rotateRightBtn:
                myRenderer.rotateRight();
                return;
            case R.id.rotateLeftBtn:
                myRenderer.rotateLeft();
                return;
        }

        Toast.makeText(v.getContext(), "to be updated", Toast.LENGTH_LONG).show();
    }

    private static String getRotationPayload(int facing_direction, int target_direction) {
        int delta_direction = GridMapUpdateManager.calculate_delta(facing_direction, target_direction);
        String payload = "";
        while (delta_direction == -90 || delta_direction == -180 || delta_direction == 270) { // turn left
            payload += "L";
            facing_direction -= 90;
            delta_direction = GridMapUpdateManager.calculate_delta(facing_direction, target_direction);
        }
        while (delta_direction == 90 || delta_direction == 180 || delta_direction == -270) { // turn right
            payload += "R";
            facing_direction += 90;
            delta_direction = GridMapUpdateManager.calculate_delta(facing_direction, target_direction);
        }

        return payload;
    }

    @SuppressLint("SetTextI18n")
    public static void moveRobot(Context context, int direction) {
//        if(is3Dmode){
//            direction = RotateDirection(direction);
//            if(direction == MOVE_NONE) return;
//        }
        int facing = (int)myRenderer.getRotation() % 360;
        String payload = "";

        switch (direction) {
            case MOVE_UP:myRenderer.setZ(myRenderer.getZ() - 1);
                if (GridMapUpdateManager.RobotDescriptor.getRowNumber() - 1 == 0) {
                    addTextToStatusWindow(((Activity) context), "ERROR: Robot cannot move up anymore\n");
                } else {
                    //SetRobotPosition(context, false);
                    addTextToStatusWindow((Activity) context, "NORTH");
                    payload += "S" + getRotationPayload(facing, GridMapUpdateManager.FacingDirection.NORTH) + "S1";
                }
                break;
            case MOVE_DOWN:myRenderer.setZ(myRenderer.getZ()+1);
                if (GridMapUpdateManager.RobotDescriptor.getRowNumber() + 1 == GridMapHandler2D.rowTotalNumber - 1) {
                    addTextToStatusWindow(((Activity) context), "ERROR: Robot cannot move down anymore\n");
                } else {
                    //SetRobotPosition(context, false);
                    addTextToStatusWindow((Activity) context, "SOUTH");
                    payload += "S" + getRotationPayload(facing, GridMapUpdateManager.FacingDirection.SOUTH) + "S1";
                }
                break;
            case MOVE_LEFT:myRenderer.setX(myRenderer.getX() - 1);
                if (GridMapUpdateManager.RobotDescriptor.getColumnNumber() - 1 == 0) {
                    addTextToStatusWindow(((Activity) context), "ERROR: Robot cannot move left anymore\n");
                } else {
                    //SetRobotPosition(context, false);
                    addTextToStatusWindow((Activity) context, "WEST");
                    payload += "S" + getRotationPayload(facing, GridMapUpdateManager.FacingDirection.WEST) + "S1";
                }
                break;
            case MOVE_RIGHT:myRenderer.setX(myRenderer.getX() + 1);
                if (GridMapUpdateManager.RobotDescriptor.getColumnNumber() + 1 == GridMapHandler2D.columnTotalNumber - 1) {
                    addTextToStatusWindow(((Activity) context), "ERROR: Robot cannot move right anymore\n");
                } else {
                    //SetRobotPosition(context, false);
                    addTextToStatusWindow((Activity) context, "EAST");
                    payload += "S" + getRotationPayload(facing, GridMapUpdateManager.FacingDirection.EAST) + "S1";
                }
                break;
        }

        if (!payload.equals("")) {
            BluetoothFragment.sendMessage(payload);
        }
    }

    public static int rotateDirection(int direction){
        int[] d = new int[4];
        int facing = (int)myRenderer.getRotation()%360;
        switch (facing){
            case 0://face north
                d[0] = MOVE_UP;
                d[1] = MOVE_DOWN;
                d[2] = MOVE_LEFT;
                d[3] = MOVE_RIGHT;
                break;
            case 90://face east
            case -270:
                d[0] = MOVE_RIGHT;
                d[1] = MOVE_LEFT;
                d[2] = MOVE_UP;
                d[3] = MOVE_DOWN;
                break;
            case 180://face south
            case -180:
                d[0] = MOVE_DOWN;
                d[1] = MOVE_UP;
                d[2] = MOVE_RIGHT;
                d[3] = MOVE_LEFT;
                break;
            case 270://face west
            case -90:
                d[0] = MOVE_LEFT;
                d[1] = MOVE_RIGHT;
                d[2] = MOVE_DOWN;
                d[3]= MOVE_UP;
                break;
            default: return MOVE_NONE; //error
        }

        switch (direction){
            case MOVE_UP:
                direction = d[0];
                break;
            case MOVE_DOWN:
                direction = d[1];
                break;
            case MOVE_LEFT:
                direction = d[2];
                break;
            case MOVE_RIGHT:
                direction = d[3];
                break;
        }
        return direction;
    }

    public static void addTextToStatusWindow(Activity activity, String stringToAdd) {
        try {
            TextView statusWindow;
            ScrollView scrollView;
            if(is3Dmode) {
                scrollView = activity.findViewById(R.id.scrollView3D);
                statusWindow = activity.findViewById(R.id.statusWindowTV3D);
            }
            else {
                scrollView = activity.findViewById(R.id.scrollView2D);
                statusWindow = activity.findViewById(R.id.statusWindowTV2D);
            }
            if(stringToAdd != null) {
                statusWindowTxt = statusWindowTxt + stringToAdd + "\n";
                statusWindowTxt = statusWindowTxt.replace("\n\n", "\n");
            }
            statusWindow.setText(statusWindowTxt);
            scrollView.fullScroll(View.FOCUS_DOWN);
        } catch (Exception e) {
            //user switched fragment
            return;
        }

    }

    /*
    //temporary variables for testing
    private static int timer = 39;
    private static Handler handler = new Handler();
    private static Runnable runnable;

    private static void RobotMovingSimulator(final View v) {
        if (handler.hasMessages(0)) {
            handler.removeCallbacks(runnable);
        } else {
            runnable = new Runnable() {
                @Override
                public void run() {
                    timer++;
                    timer = timer % 40;

                    if (timer < 10) {
                        GridMapHandler2D.robotCurrentRow--;
                        SetArrowPicture(v.getContext(), 0, GridMapHandler2D.robotCurrentRow, 4);
                        SetArrowPicture(v.getContext(), 90, GridMapHandler2D.robotCurrentRow, 5);
                    } else if (timer < 20) {
                        GridMapHandler2D.robotCurrentColumn++;
                        changeCellColor(v.getContext(), Color.BLACK, 2, GridMapHandler2D.robotCurrentColumn);
                    } else if (timer < 30) {
                        GridMapHandler2D.robotCurrentRow++;
                        RemoveArrowPicture(v.getContext(), GridMapHandler2D.robotCurrentRow, 4);
                        RemoveArrowPicture(v.getContext(), GridMapHandler2D.robotCurrentRow, 5);
                    } else {
                        GridMapHandler2D.robotCurrentColumn--;
                        changeCellColor(v.getContext(), Color.WHITE, 2, GridMapHandler2D.robotCurrentColumn);
                    }

                    setRobotPosition(v.getContext(), false);

                    handler.postDelayed(this, 100);
                }
            };
            handler.postDelayed(runnable, 100);
        }
    }*/
}
