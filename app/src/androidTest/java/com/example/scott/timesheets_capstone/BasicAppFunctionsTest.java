package com.example.scott.timesheets_capstone;


import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.DatePicker;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class BasicAppFunctionsTest {
    @Rule
    public IntentsTestRule<ContractListActivity> mActivityTestRule =
            new IntentsTestRule<>(ContractListActivity.class);

    @Test
    public void createNewContractandCheckTimesheetViews() {

        onView(withId(R.id.fab))
                .perform(click());

        intended(hasComponent(NewContractActivity.class.getName()));

        onView(withId(R.id.company_name_edit))
                .perform(typeText("Udacity"));
        onView(withId(R.id.manager_name_edit))
                .perform(typeText("Joe Bloggs"));
        onView(withId(R.id.manager_email_edit))
                .perform(typeText("Joe.Bloggs@gmail.com"));
        onView(withId(R.id.default_hours_edit))
                .perform(typeText("8"), pressImeActionButton());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.default_days_edit))
                .perform(click());
        onView(withId(R.id.tD))
                .perform(click());
        onView(withId(R.id.set_days_button))
                .perform(click());
        onView(withId(R.id.start_date_edit))
                .perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2018, 11, 11));
        onView(withText("OK")).perform(click());
        onView(withId(R.id.create_contract_button)).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.contract_list)).perform(RecyclerViewActions.scrollToPosition(0))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.fab))
                .perform(click());
        intended(hasComponent(TimesheetDetailActivity.class.getName()));

        onView(withId(R.id.week_commence_date_tv))
                .check(matches(isDisplayed()));
        onView(withId(R.id.monday_edit))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tuesday_edit))
                .check(matches(isDisplayed()));
        onView(withId(R.id.wednesday_edit))
                .check(matches(isDisplayed()));
        onView(withId(R.id.thursday_edit))
                .check(matches(isDisplayed()));
        onView(withId(R.id.friday_edit))
                .check(matches(isDisplayed()));
        onView(withId(R.id.saturday_edit))
                .check(matches(isDisplayed()));
        onView(withId(R.id.sunday_edit))
                .check(matches(isDisplayed()));
    }

}
