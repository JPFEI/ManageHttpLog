

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ManageTool extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JLabel lbType;
	private JList listPath;
	private JList listSubpath;
	private JList listFileName;
	private JComboBox cmbType;
	private JTextArea txtContent;
	private JMenu menuConfig;

	private HashMap<String,HashMap<String,ArrayList<String>>> dateHostFile;
	private HashMap<String,HashMap<String,ArrayList<String>>> hostDateFile;
	private HashMap<String,HashMap<String,ArrayList<String>>> curentDateFile;
	private ArrayList<String> pathNameList;
	
	String logPath = "";
	static String infFileName = "path.inf";
	public ManageTool() {
		super("��־����");

		this.dateHostFile = new HashMap<String, HashMap<String, ArrayList<String>>>();
		this.hostDateFile = new HashMap<String, HashMap<String, ArrayList<String>>>();
		this.pathNameList = new ArrayList<String>();
		
		JPanel contect = new JPanel(new GridLayout(4, 0));

//		p.setLayout(new GridLayout(1, 0));
		GridBagLayout layout =  new GridBagLayout();
		this.setLayout(layout);

		JPanel typeContect = new JPanel(new GridLayout(4, 0));
		JButton btn = new JButton("ˢ������");
		btn.addMouseListener(new ManageToolMouseListener());
		lbType = new JLabel("����");
		lbType.setSize(80, 100);
		// ����һ��û��ѡ�����Ͽ�
		cmbType = new JComboBox();//JComboBox({"ʱ�仮��","ʱ�仮��"});
		cmbType.addItem("ʱ�仮��");
		cmbType.addItem("����������");
		cmbType.addItemListener(new TypeListener());
		typeContect.add(new JPanel());
		typeContect.add(cmbType);
		typeContect.add(btn);
		// �����б�򣬲�������ѡ����listPath�����е�ʡ��
		listPath = new JList();
		// �����б��ֻ�ܵ�ѡ
		listPath.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// ���б����ע�����
		listPath.addListSelectionListener(new FilePathListener());

		listSubpath = new JList();
		listSubpath.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listSubpath.addListSelectionListener(new FileSubpathListener());
		listFileName = new JList();
		listFileName.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listFileName.addListSelectionListener(new FileListener());

		txtContent = new JTextArea(50, 30);
		txtContent.setBackground(Color.lightGray);
		contect.setBackground(Color.lightGray);
		contect.setSize(100, 600);
		contect.add(typeContect);
		JScrollPane jsc = new JScrollPane(listPath);
		contect.add(jsc);
		jsc = new JScrollPane(listSubpath);
		contect.add(jsc);
		jsc = new JScrollPane(listFileName);
		contect.add(jsc);
//		contect.add(txtContent);
		this.add(contect);
		jsc = new JScrollPane(txtContent);
		this.add(jsc);
		this.setSize(760,600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		this.setResizable(false);
		
		GridBagConstraints s= new GridBagConstraints();//����һ��GridBagConstraints��
        //������������ӽ����������ʾλ��
        s.fill = GridBagConstraints.BOTH;
        //�÷�����Ϊ���������������ڵ�������������Ҫ��ʱ����ʾ���
        //NONE�������������С��
        //HORIZONTAL���ӿ������ʹ����ˮƽ��������������ʾ���򣬵��ǲ��ı�߶ȡ�
        //VERTICAL���Ӹ������ʹ���ڴ�ֱ��������������ʾ���򣬵��ǲ��ı��ȡ�
        //BOTH��ʹ�����ȫ��������ʾ����
        s.gridwidth=1;//�÷������������ˮƽ��ռ�õĸ����������Ϊ0����˵��������Ǹ��е����һ��
        s.weightx = 0;//�÷����������ˮƽ��������ȣ����Ϊ0��˵�������죬��Ϊ0�����Ŵ�������������죬0��1֮��
        s.weighty=0;//�÷������������ֱ��������ȣ����Ϊ0��˵�������죬��Ϊ0�����Ŵ�������������죬0��1֮��
        layout.setConstraints(contect, s);//�������
        s.gridwidth=6;
        s.weightx = 0.5;
        s.weighty=0.1;
        layout.setConstraints(jsc, s);
        
        JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		menuConfig = new JMenu("����");
		JMenuItem itemConfig  = new JMenuItem("����·��");
		menuConfig.add(itemConfig);
		menuBar.add(menuConfig);
		// ע�����
		itemConfig.addActionListener(this);
        
        //�������ļ���ȡ��־·���������������������ã������������
		File infoFile = new File(infFileName);
		if(infoFile.exists()){
	        if(getPathFromInfo() == null){
	            resetLogPath();
	        }else{
	        	reloadData();
	        }
		}else{
            resetLogPath();
		}
	}
	
	private void refishPathData(){
		Set<String> pathSet = this.curentDateFile.keySet();
		String[] pathList =  pathSet.toArray(new String[pathSet.size()]);
		listPath.setListData(pathList);
		listPath.setSelectedIndex(0);
	}
	
	public void reloadData(){
		setData4Views();
		setHostDateFile();
		this.curentDateFile = dateHostFile;//hostDateFile
		refishPathData();
	}
	
	private void resetLogPath(){

		JFileChooser fc = new JFileChooser();
		// ��ʾ�ļ��򿪶Ի���
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//ֻ��ѡ��Ŀ¼ 
		int rVal = fc.showOpenDialog(this);
		// ������ȷ��(Yes/OK)
		if (rVal == JFileChooser.APPROVE_OPTION) {
			// ��ȡ�ļ��Ի������û�ѡ�е��ļ���
			File chooseFile = fc.getSelectedFile();
			logPath = chooseFile.getPath();
			saveLogPathForInfo(logPath);
			reloadData();
		}else{
			// ϵͳ�˳�
			System.exit(0);
		}
	}
	
	private void setData4Views(){
		File file = new File(logPath);
		this.dateHostFile = new HashMap<String, HashMap<String, ArrayList<String>>>();
		// �õ��ļ����б�
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for(int i=0;i<files.length;i++){
				File file1 = files[i];
				if (file1.isDirectory()) {
					File[] files1 = file1.listFiles();
					HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
					for(int j=0;j<files1.length;j++){
						File file2 = files1[j];
						ArrayList<String> fileNames = new ArrayList<String>();
						pathNameList.add(file1.getName()+"/"+file2.getName());
						if (file2.isDirectory()) {
							File[] files2 = file2.listFiles();
							for(int k=0;k<files2.length;k++){
								File file3 = files2[k];
								fileNames.add(file3.getName());
							}
							map.put(file2.getName(), fileNames);
						}
					}
					this.dateHostFile.put(file1.getName(), map);
				}
			}
		}
	}
	
	private void setHostDateFile(){
		this.hostDateFile = new HashMap<String, HashMap<String, ArrayList<String>>>();
		Set<String> hostSet = new HashSet<String>();//dateHostFile.values().keySet();//hostMap.keySet();
		Collection<HashMap<String, ArrayList<String>>> hostValuesMap = dateHostFile.values();
		for(HashMap<String, ArrayList<String>> map:hostValuesMap){
			hostSet.addAll( map.keySet());
		}
		System.out.println("pathNameList:"+pathNameList);
		Set<String> dateSet = dateHostFile.keySet();
		for(String hostName:hostSet){
			HashMap<String, ArrayList<String>> dateMap = new HashMap<String, ArrayList<String>>();
			for(String datePath:dateSet){
				HashMap<String, ArrayList<String>> hostMap = dateHostFile.get(datePath);
				if(pathNameList.contains(datePath+"/"+hostName)){
					dateMap.put(datePath, hostMap.get(hostName));
				}
			}
			hostDateFile.put(hostName, dateMap);
		}
	}

	//��ʱ�䣬��������
	private void setViewData4DateHost(){
		this.curentDateFile = dateHostFile;
	}

	//��������ʱ�����
	private void setViewData4HostDate(){
		this.curentDateFile = hostDateFile;//
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object source = e.getSource();
		String cmd = e.getActionCommand();
		System.out.println(e.getActionCommand());
		System.out.println(source);
		if (cmd == "����·��") {
			resetLogPath();
		}
	}
	
	private class TypeListener implements ItemListener {
		
		public void itemStateChanged(ItemEvent e) {
			// TODO Auto-generated method stub
			int i = cmbType.getSelectedIndex();
			switch (i) {
			case 0:
				setViewData4DateHost();
				break;
			case 1:
				setViewData4HostDate();
				break;
			}
			refishPathData();
		}
	}
	
	// ����һ���б�ѡ�������
	private class FilePathListener implements ListSelectionListener {
		private int lastSelectedIndex = -1;
		
		public void valueChanged(ListSelectionEvent e) {
			if(listPath.getSelectedIndex() != lastSelectedIndex){
				String path = (String)listPath.getSelectedValue();
				if(path == null){
					return;
				}
				//System.out.println(path);
				//System.out.println(curentDateFile);
				HashMap<String, ArrayList<String>> map = curentDateFile.get(path);
				Set<String> pathSet = map.keySet();
				String[] pathArr =  pathSet.toArray(new String[pathSet.size()]);
				listSubpath.setListData(pathArr);
				listSubpath.setSelectedIndex(0);
				txtContent.setText("");
				lastSelectedIndex = listPath.getSelectedIndex();
			}
		}
	}

	private class FileSubpathListener implements ListSelectionListener {
		private int lastSelectedIndex = -1;
		
		public void valueChanged(ListSelectionEvent e) {
			if(listSubpath.getSelectedIndex() != lastSelectedIndex){
				String path = (String)listPath.getSelectedValue();
				String subpath = (String)listSubpath.getSelectedValue();
				HashMap<String, ArrayList<String>> map = curentDateFile.get(path);
				ArrayList<String> pathList = map.get(subpath);
				if(pathList != null){
					String[] fileArr =  pathList.toArray(new String[pathList.size()]);
					listFileName.setListData(fileArr);
				}else{
					listFileName.setListData(new String[0]);
				}
				txtContent.setText("");
				lastSelectedIndex = listSubpath.getSelectedIndex();
			}
		}
	}
	
	private class FileListener implements ListSelectionListener {
		private int lastSelectedIndex = -1;
		
		public void valueChanged(ListSelectionEvent e) {
			if(listFileName.getSelectedIndex() != lastSelectedIndex){
				String path = (String)listPath.getSelectedValue();
				String subpath = (String)listSubpath.getSelectedValue();
				String fileName = (String)listFileName.getSelectedValue();
//				System.out.println(path);
//				System.out.println(listSubpath.getSelectedValue());
				HashMap<String, ArrayList<String>> map = curentDateFile.get(path);
				ArrayList<String> pathList = map.get(subpath);
				String[] fileArr =  pathList.toArray(new String[pathList.size()]);
				listFileName.setListData(fileArr);
				if(cmbType.getSelectedIndex() == 0){
					openFile(logPath+"/"+path+"/"+subpath+"/"+fileName);
				}else{
					openFile(logPath+"/"+subpath+"/"+path+"/"+fileName);
				}
				lastSelectedIndex = listFileName.getSelectedIndex();
			}
		}
	}
	
	private void saveLogPathForInfo(String path){
		ObjectOutputStream obs = null;
		try {
			// ����һ��ObjectOutputStream�Ķ���
			obs = new ObjectOutputStream(new FileOutputStream(infFileName));
			// �Ѷ���д�뵽�ļ���
			obs.writeObject(path);
			obs.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				obs.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private String getPathFromInfo(){
		String logPathStr = null;
		ObjectInputStream ois = null;
		try {
			// ����һ��ObjectInputStream�Ķ���,���з����л�
			ois = new ObjectInputStream(new FileInputStream(infFileName));
			Object obj = ois.readObject();
			if (obj != null) {
				logPathStr = (String) obj;
//				System.out.println("�����ļ���" + logPathStr);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		this.logPath = logPathStr;
		return logPathStr;
	}

	// ���ļ��ķ���
	private void openFile(String filePath) {
		System.out.println("filePath:" + filePath);
		try {
			// ����һ���ļ������������ڶ��ļ�
			// FileReader fread = new FileReader(filePath);
			BufferedReader bread = new BufferedReader(new InputStreamReader(
					new FileInputStream(filePath), "UTF-8"));
			// ����һ��������
			// BufferedReader bread = new BufferedReader(fread);
			// ���ļ��ж�һ����Ϣ
			String line = bread.readLine();
			StringBuffer buf = new StringBuffer();
			buf.append(line);
			// ѭ�����ļ��е����ݣ�����ʾ���ı�����
			while (line != null) {
				// ����һ��
				buf.append(line + "\n");
				line = bread.readLine();
			}
			txtContent.setText(buf.toString());
			bread.close();
			// fread.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//�̳�MouseAdapter��������
	private class ManageToolMouseListener implements MouseListener {

		/*public ManageToolMouseAdapter() {
			System.out.println("ManageToolMouseAdapter");
		}*/

		// ��д��������¼�������
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			reloadData();
	        JButton btn = (JButton) e.getSource();
			System.out.println("reloadData");
			System.out.println(btn.getName());
		}

		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}

	/*public static void main(String args[]) {
		new ManageToolMouseAdapter();
	}*/
	public static void main(String[] args) {
		ManageTool f = new ManageTool();
		f.setVisible(true);
	}
}
