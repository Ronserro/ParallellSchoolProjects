package Model;

import java.util.*;

/**@author Christian Lind, Oliver Andersson, Markus Grahn
 * Uses Employee, WorkShift, WorkDay. Used by Admin
 * A class that handles the sortation of employees
 */
public class EmployeeSorter implements Comparator<WorkShift> {
    public ArrayList<WorkShift> workShifts = new ArrayList<>();
    public HashMap<WorkShift, List<Employee>> potentialWorkShiftCandidate = new HashMap<>();

    @Override
    public int compare(WorkShift a, WorkShift b) {
        return Integer.compare(potentialWorkShiftCandidate.get(a).size(), potentialWorkShiftCandidate.get(b).size());
    }

    public void sortPotentialWorkShiftCandidate(List<Employee> employees, List<WorkDay> workDays) {
        ArrayList<Certificate> certificates = new ArrayList<>();
        for (WorkDay workDay : workDays) {
            for (int j = 0; j < workDay.getDepartmentSize(); j++) {
                for (WorkShift ws : workDay.getWorkShifts(workDay.getDepartment(j))) {
                    certificates.clear();
                    for (int k1 = 0; k1 < ws.getCertificatesSize(); k1++) {
                        certificates.add(ws.getCertificate(k1));
                    }
                    workShifts.add(ws);
                    potentialWorkShiftCandidate.put(ws, getAvailableQualifiedPersonnel(employees, certificates, ws.START, ws.END));
                }
            }
        }
    }

    public void delegateEmployeeToWorkshift() {
        Date d = new Date();
        boolean isAllOccupied = true;
        for (WorkShift workShift : workShifts) {
            d.setTime(workShift.START);
            WorkDay workday = OurCalendar.getInstance().getDate(d);
            potentialWorkShiftCandidate.get(workShift).sort(Comparator.comparing(Employee::getOccupiedTimesSize));
            for (Employee employee : potentialWorkShiftCandidate.get(workShift)) {
                if (!employee.isOccupied(workShift.START, workShift.END) && !workShift.isOccupied()) {
                    workday.occupiesEmployee(workShift, employee);
                }
            }
            if (!workShift.isOccupied()) {
                isAllOccupied = false;

            }
        }
        if (!isAllOccupied) {
            new SendNotification("smtp.gmail.com", "random@gmail.com", "random@gmail.com", "*****", "Workshifts not filled", getEmptyWorkShifts());
        }

    }

    private List<WorkShift> getEmptyWorkShifts() {
        ArrayList<WorkShift> notFilled = new ArrayList<>();
        for (WorkShift workShift : workShifts) {
            if (!workShift.isOccupied()) {
                notFilled.add(workShift);
            }
        }
        return notFilled;
    }

    public List<Employee> getPotentialEmployees(WorkShift workShift) {
        return potentialWorkShiftCandidate.get(workShift);
    }

    /**
     * Gets a list of employees that are both available at a given time span and are qualified with certain certificates
     *
     * @param employees    A list with employees that the method selects from
     * @param certificates A list with certificates
     * @param start        Start time of the time span
     * @param stop         Stop time of the time span
     * @return A list with employees that are available and qualified
     */
    public static List<Employee> getAvailableQualifiedPersonnel(List<Employee> employees, List<Certificate> certificates, long start, long stop) {
        List<Employee> newList = new ArrayList<>();
        for (Employee e : employees) {
            ArrayList<Certificate> tempList = new ArrayList<>();
            for (int i = 0; i < e.getCertificatesSize(); i++) {
                tempList.add(e.getCertificate(i).getCertificate());
            }
            if (tempList.containsAll(certificates) && !e.isOccupied(start, stop))
                newList.add(e);
        }

        return newList;
    }

    /**
     * Gets a list of employees that are available at a given time span
     *
     * @param start        Start time of the time span
     * @param end          Stop time of the time span
     * @param employeeList A list with employees that the method selects from
     * @return A list with employees that are available
     */
    public List<Employee> getAvailablePersons(long start, long end, List<Employee> employeeList) { //skickar in lista med anställda i parametern för att kunna göra denna och getQualifiedPersons i valfri ordning
        List<Employee> availableList = new ArrayList<>();
        for (Employee e : employeeList)
            if (!e.isOccupied(start, end))
                availableList.add(e);
        return availableList;
    }

    /**
     * Gets a list of employees that are qualified for a certain workshift
     *
     * @param ws           A workshift
     * @param employeeList A list with employees that the method selects from
     * @return A list with employees that are qualified for the workshift
     */
    public List<Employee> getQualifiedPersons(WorkShift ws, List<Employee> employeeList) {
        List<Employee> qualifiedList = new ArrayList<>();
        for (Employee e : employeeList)
            if (e.isQualified(ws))
                qualifiedList.add(e);
        return qualifiedList;
    }
}
