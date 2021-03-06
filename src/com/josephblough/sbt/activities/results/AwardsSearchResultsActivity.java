package com.josephblough.sbt.activities.results;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.josephblough.sbt.ApplicationController;
import com.josephblough.sbt.R;
import com.josephblough.sbt.adapters.AwardDataAdapter;
import com.josephblough.sbt.callbacks.AwardsRetrieverCallback;
import com.josephblough.sbt.criteria.AwardsSearchCriteria;
import com.josephblough.sbt.data.Award;
import com.josephblough.sbt.tasks.AwardsRetrieverTask;
import com.josephblough.sbt.tasks.PdfCheckerTask;

public class AwardsSearchResultsActivity extends SearchResultsActivity implements AwardsRetrieverCallback, OnItemClickListener {

    public final static String SEARCH_CRITERIA_EXTRA = "AwardsSearchResultsActivity.SearchCriteria";
    
    private List<Award> data = null;
    private AwardsSearchCriteria criteria = null;
    private ProgressDialog progress;
    
    private TextView titleLabel;
    private TextView urlLabel;
    private TextView abstractLabel;
    private TextView agencyLabel;
    private TextView programLabel;
    private TextView phaseLabel;
    private TextView yearLabel;
    private TextView companyLabel;
    private TextView institutionLabel;
    
    private TableRow titleRow;
    private TableRow urlRow;
    private TableRow abstractRow;
    private TableRow agencyRow;
    private TableRow programRow;
    private TableRow phaseRow;
    private TableRow yearRow;
    private TableRow companyRow;
    private TableRow institutionRow;
    
    private Button dismissDetailsButton;
    private Button visitUrlButton;
    private Button shareUrlButton;
    private View detailsView;
    private View detailsControls;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.awards_search_results);
	
	criteria = getIntent().getParcelableExtra(SEARCH_CRITERIA_EXTRA);
	
	if (data == null && criteria != null) {
	    progress = ProgressDialog.show(this, "Loading...", "Retrieving Awards", true, true);
	    new AwardsRetrieverTask(this).execute(criteria);
	}
	getListView().setOnItemClickListener(this);
	
	detailsView = findViewById(R.id.awards_details_table);
	detailsControls = findViewById(R.id.detail_controls);
	dismissDetailsButton = (Button)findViewById(R.id.detail_controls_dismiss_details);
	visitUrlButton = (Button)findViewById(R.id.detail_controls_visit_link);
	shareUrlButton = (Button)findViewById(R.id.detail_controls_share_link);

	titleLabel = (TextView)findViewById(R.id.awards_details_title_value);
	urlLabel = (TextView)findViewById(R.id.awards_details_url_value);
	abstractLabel = (TextView)findViewById(R.id.awards_details_abstract_value);
	agencyLabel = (TextView)findViewById(R.id.awards_details_agency_value);
	programLabel = (TextView)findViewById(R.id.awards_details_program_value);
	phaseLabel = (TextView)findViewById(R.id.awards_details_phase_value);
	yearLabel = (TextView)findViewById(R.id.awards_details_year_value);
	companyLabel = (TextView)findViewById(R.id.awards_details_company_value);
	institutionLabel = (TextView)findViewById(R.id.awards_details_institution_value);

	titleRow = (TableRow)findViewById(R.id.awards_details_title_row);
	urlRow = (TableRow)findViewById(R.id.awards_details_url_row);
	abstractRow = (TableRow)findViewById(R.id.awards_details_abstract_row);
	agencyRow = (TableRow)findViewById(R.id.awards_details_agency_row);
	programRow = (TableRow)findViewById(R.id.awards_details_program_row);
	phaseRow = (TableRow)findViewById(R.id.awards_details_phase_row);
	yearRow = (TableRow)findViewById(R.id.awards_details_year_row);
	companyRow = (TableRow)findViewById(R.id.awards_details_company_row);
	institutionRow = (TableRow)findViewById(R.id.awards_details_institution_row);
	
	abstractLabel.setMovementMethod(ScrollingMovementMethod.getInstance());
	
	dismissDetailsButton.setOnClickListener(new View.OnClickListener() {
	    
	    public void onClick(View v) {
		detailsView.setVisibility(View.GONE);
		detailsControls.setVisibility(View.GONE);
	    }
	});
	
	getListView().setFastScrollEnabled(true);
	getListView().setTextFilterEnabled(true);
	
	// Set a long click handler
	getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

	    public boolean onItemLongClick(AdapterView<?> parent, View view,
		    int position, long id) {
		hideSearch();
		
		Award award = ((AwardDataAdapter)getListAdapter()).getItem(position);
		showDetails(award);
		share(award);
		
		return true;
	    }
	});
    }
    
    public void share(final Award award) {
	Intent sharingIntent = new Intent(Intent.ACTION_SEND);
	sharingIntent.setType("text/plain");
	sharingIntent.putExtra(Intent.EXTRA_SUBJECT, Html.fromHtml(award.title).toString());
	sharingIntent.putExtra(Intent.EXTRA_TEXT, award.formatForSharing());
	startActivity(Intent.createChooser(sharingIntent,"Share using"));
    }
    
    public void success(List<Award> results) {
	this.data = results;
	removeInvalidResults();
	
	Collections.sort(this.data, new Comparator<Award>() {

	    public int compare(Award award1, Award award2) {
		return award1.title.compareTo(award2.title);
	    }
	});

	AwardDataAdapter adapter = new AwardDataAdapter(this, this.data);
	setListAdapter(adapter);
	
	if (progress != null) {
	    progress.dismiss();
	    progress = null;
	}
	
	if (this.data == null || this.data.size() == 0)
	    Toast.makeText(this, R.string.no_data_returned, Toast.LENGTH_LONG).show();
	else {
	    if (((ApplicationController)getApplicationContext()).shouldShowTooltip(R.string.filter_results_tooltip))
		Toast.makeText(this, R.string.filter_results_tooltip, Toast.LENGTH_LONG).show();
	}
    }

    public void error(String error) {
	if (progress != null) {
	    progress.dismiss();
	    progress = null;
	}
	
	Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
	Toast.makeText(this, R.string.too_much_data, Toast.LENGTH_LONG).show();
	finish();
    }
    
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	hideSearch();

	final Award selectedItem = (Award)getListAdapter().getItem(position);
	showDetails(selectedItem);
    }
    
    private boolean isEmpty(final String value) {
	return value == null || "".equals(value) || "null".equals(value);
    }
    
    private void showDetails(final Award award) {
	if (!isEmpty(award.title)) {
	    titleLabel.setText(Html.fromHtml(award.title));
	    titleRow.setVisibility(View.VISIBLE);
	}
	else {
	    titleRow.setVisibility(View.GONE);
	}
	
	// URL
	urlRow.setVisibility(View.GONE);
	if (!isEmpty(award.link)) {
	    urlLabel.setText(award.link);
	    //urlRow.setVisibility(View.VISIBLE);
	}
	else
	    urlRow.setVisibility(View.GONE);
	
	// Abstract
	if (!isEmpty(award.awardAbstract)) {
	    abstractLabel.setText(Html.fromHtml(award.awardAbstract));
	    // Reset the label scroll to the top
	    abstractLabel.scrollTo(0, 0);
	    abstractRow.setVisibility(View.VISIBLE);
	}
	else
	    abstractRow.setVisibility(View.GONE);
	
	// Agency
	if (!isEmpty(award.agency)) {
	    agencyLabel.setText(award.agency);
	    agencyRow.setVisibility(View.VISIBLE);
	}
	else
	    agencyRow.setVisibility(View.GONE);
	
	// Program
	if (!isEmpty(award.program)) {
	    programLabel.setText(award.program);
	    programRow.setVisibility(View.VISIBLE);
	}
	else
	    programRow.setVisibility(View.GONE);
	
	// Phase
	if (award.phase != null) {
	    phaseLabel.setText(award.phase.toString());
	    phaseRow.setVisibility(View.VISIBLE);
	}
	else
	    programRow.setVisibility(View.GONE);
	
	// Year
	if (award.year != null) {
	    yearLabel.setText(award.year.toString());
	    yearRow.setVisibility(View.VISIBLE);
	}
	else
	    yearRow.setVisibility(View.GONE);
	
	// Company
	if (!isEmpty(award.company)) {
	    companyLabel.setText(award.company);
	    companyRow.setVisibility(View.VISIBLE);
	}
	else
	    companyRow.setVisibility(View.GONE);
	
	// Research Institution
	if (!isEmpty(award.researchInstitution)) {
	    institutionLabel.setText(award.researchInstitution);
	    institutionRow.setVisibility(View.VISIBLE);
	}
	else
	    institutionRow.setVisibility(View.GONE);
	

	detailsView.setVisibility(View.VISIBLE);
	detailsControls.setVisibility(View.VISIBLE);
	
	visitUrlButton.setOnClickListener(new View.OnClickListener() {
	    
	    public void onClick(View v) {
		visitData(award);
	    }
	});
	
	shareUrlButton.setOnClickListener(new View.OnClickListener() {
	    
	    public void onClick(View v) {
		share(award);
	    }
	});
    }
    
    private void visitData(final Award award) {
	// Display a warning message to the user if this is a PDF document
	new PdfCheckerTask(this).execute(award.link);
	
	// Launch the document
	final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(award.link));
	startActivity(intent);
    }
    
    private void removeInvalidResults() {
	Iterator<Award> it = this.data.iterator();
	while (it.hasNext()) {
	    Award award = it.next();
	    if (award.title == null || "".equals(award.title) ||
		    award.link == null || "".equals(award.link)) {
		it.remove();
	    }
	}
    }
    
    protected boolean isDetailsViewShowing() {
	return detailsView.isShown();
    }
    
    protected void hideDetailsView() {
	detailsView.setVisibility(View.GONE);
	detailsControls.setVisibility(View.GONE);
    }
}
