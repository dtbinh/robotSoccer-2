package data;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import ui.SituationArea;

public class SituationTableModel extends AbstractTableModel {
	
	//for now the model we use the list of area, later on we could create
	//situation object which contain the area and other information relate to the situations
	
	private ArrayList<Situation> listOfSituations;
	
	public SituationTableModel(ArrayList<Situation> list) {
		listOfSituations = list;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return listOfSituations.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		// TODO Auto-generated method stub
		return listOfSituations.get(row);
	}
	
	public String getColumnName(int col) {
		if (col == 0) {
			return "Situation Name";
		}
		else {
			return "";
		}
    }
	
	
	public boolean isCellEditable(int row, int col) { 
	    return true; 
	}
	
	public void setValueAt(Object value, int row, int col) {
	    Situation s = listOfSituations.get(row);   
	    s.setSituationName((String)value);
	    fireTableCellUpdated(row, col);
	    
	 }

}