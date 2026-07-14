package bookmgmt.vpplugin;

import java.awt.Point;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.DiagramManager;
import com.vp.plugin.diagram.IClassDiagramUIModel;
import com.vp.plugin.diagram.IDiagramTypeConstants;
import com.vp.plugin.diagram.IInteractionDiagramUIModel;
import com.vp.plugin.diagram.shape.IActivationUIModel;
import com.vp.plugin.diagram.shape.IClassUIModel;
import com.vp.plugin.diagram.shape.IInteractionLifeLineUIModel;
import com.vp.plugin.diagram.shape.INoteUIModel;
import com.vp.plugin.diagram.shape.IPackageUIModel;
import com.vp.plugin.diagram.connector.IMessageUIModel;
import com.vp.plugin.model.IActivation;
import com.vp.plugin.model.IAssociation;
import com.vp.plugin.model.IAssociationEnd;
import com.vp.plugin.model.IAttribute;
import com.vp.plugin.model.IClass;
import com.vp.plugin.model.IDependency;
import com.vp.plugin.model.IFrame;
import com.vp.plugin.model.IGeneralization;
import com.vp.plugin.model.IInteractionLifeLine;
import com.vp.plugin.model.IMessage;
import com.vp.plugin.model.INOTE;
import com.vp.plugin.model.IOperation;
import com.vp.plugin.model.IPackage;
import com.vp.plugin.model.IParameter;
import com.vp.plugin.model.IRealization;
import com.vp.plugin.model.factory.IModelElementFactory;

/**
 * Builds Class Diagram + Sequence Diagrams for Manage Book (J2.S.P0113)
 * into the currently opened Visual Paradigm project.
 */
public class DiagramGenerator {

	private final DiagramManager dm = ApplicationManager.instance().getDiagramManager();
	private final IModelElementFactory factory = IModelElementFactory.instance();
	private final Map<String, IClass> classes = new LinkedHashMap<String, IClass>();
	private final Map<String, IClassUIModel> classShapes = new LinkedHashMap<String, IClassUIModel>();
	private final Map<String, IPackageUIModel> packageShapes = new LinkedHashMap<String, IPackageUIModel>();

	public void generateAll() {
		createClassDiagram();
		createAddBookSequence();
		createRemoveBookSequence();
		ApplicationManager.instance().getProjectManager().saveProject();
		System.out.println("[BookMgmt] Diagrams generated and project saved.");
	}

	// -------------------------------------------------------------------------
	// CLASS DIAGRAM
	// -------------------------------------------------------------------------
	private void createClassDiagram() {
		IClassDiagramUIModel diagram = (IClassDiagramUIModel) dm.createDiagram(DiagramManager.DIAGRAM_TYPE_CLASS_DIAGRAM);
		diagram.setName("Class Diagram - Book Management N-Layer");

		IPackage root = factory.createPackage();
		root.setName("bookmanagement");
		IPackageUIModel rootShape = (IPackageUIModel) dm.createDiagramElement(diagram, root);
		rootShape.setBounds(40, 40, 1680, 1100);
		rootShape.setRequestResetCaption(true);

		IPackage pkgPresentation = addPackage(root, "presentation", 60, 80, 320, 280, diagram, rootShape);
		IPackage pkgController = addPackage(root, "controller", 420, 80, 300, 280, diagram, rootShape);
		IPackage pkgService = addPackage(root, "service", 760, 80, 420, 420, diagram, rootShape);
		IPackage pkgDao = addPackage(root, "dao", 1220, 80, 420, 320, diagram, rootShape);
		IPackage pkgUtil = addPackage(root, "util", 1220, 430, 420, 260, diagram, rootShape);
		IPackage pkgModel = addPackage(root, "model", 60, 540, 1120, 420, diagram, rootShape);

		// --- Model ---
		IClass book = createClass(pkgModel, "Book", false);
		addAttr(book, "bookId", "int", IAttribute.VISIBILITY_PRIVATE);
		addAttr(book, "bookCode", "String", IAttribute.VISIBILITY_PRIVATE);
		addAttr(book, "bookName", "String", IAttribute.VISIBILITY_PRIVATE);
		addAttr(book, "author", "String", IAttribute.VISIBILITY_PRIVATE);
		addAttr(book, "publisher", "String", IAttribute.VISIBILITY_PRIVATE);
		addAttr(book, "publishYear", "int", IAttribute.VISIBILITY_PRIVATE);
		addAttr(book, "forRent", "boolean", IAttribute.VISIBILITY_PRIVATE);
		addOp(book, "getBookId", "int");
		addOp(book, "setBookId", "void", param("bookId", "int"));
		addOp(book, "getBookCode", "String");
		addOp(book, "setBookCode", "void", param("bookCode", "String"));
		addOp(book, "getBookName", "String");
		addOp(book, "setBookName", "void", param("bookName", "String"));
		addOp(book, "getAuthor", "String");
		addOp(book, "setAuthor", "void", param("author", "String"));
		addOp(book, "getPublisher", "String");
		addOp(book, "setPublisher", "void", param("publisher", "String"));
		addOp(book, "getPublishYear", "int");
		addOp(book, "setPublishYear", "void", param("publishYear", "int"));
		addOp(book, "isForRent", "boolean");
		addOp(book, "setForRent", "void", param("forRent", "boolean"));
		addOp(book, "toString", "String");
		placeClass(diagram, pkgModel, "Book", book, 30, 40, 240, 340, rootShape);

		IClass exBase = createClass(pkgModel, "Exception", false);
		// represent java.lang.Exception as external
		exBase.addStereotype("external");
		placeClass(diagram, pkgModel, "Exception", exBase, 320, 40, 140, 50, rootShape);

		IClass valEx = createClass(pkgModel, "ValidationException", false);
		addOp(valEx, "ValidationException", "", param("message", "String"));
		placeClass(diagram, pkgModel, "ValidationException", valEx, 500, 40, 220, 70, rootShape);

		IClass dupEx = createClass(pkgModel, "DuplicateBookException", false);
		addOp(dupEx, "DuplicateBookException", "", param("message", "String"));
		placeClass(diagram, pkgModel, "DuplicateBookException", dupEx, 500, 140, 240, 70, rootShape);

		IClass notFoundEx = createClass(pkgModel, "BookNotFoundException", false);
		addOp(notFoundEx, "BookNotFoundException", "", param("message", "String"));
		placeClass(diagram, pkgModel, "BookNotFoundException", notFoundEx, 500, 240, 240, 70, rootShape);

		generalize(diagram, exBase, valEx);
		generalize(diagram, exBase, dupEx);
		generalize(diagram, exBase, notFoundEx);

		// --- Observer ---
		IClass listener = createClass(pkgService, "BookChangeListener", true);
		listener.addStereotype("Observer");
		addOp(listener, "onBooksChanged", "void");
		placeClass(diagram, pkgService, "BookChangeListener", listener, 20, 30, 180, 70, rootShape);

		// --- Service ---
		IClass iService = createClass(pkgService, "IBookService", true);
		addOp(iService, "getAllBooks", "List<Book>");
		addOp(iService, "getBookByCode", "Book", param("bookCode", "String"));
		addOp(iService, "addBook", "void", param("book", "Book"));
		addOp(iService, "updateBook", "void", param("book", "Book"));
		addOp(iService, "removeBook", "void", param("bookCode", "String"));
		addOp(iService, "addBookChangeListener", "void", param("listener", "BookChangeListener"));
		addOp(iService, "removeBookChangeListener", "void", param("listener", "BookChangeListener"));
		placeClass(diagram, pkgService, "IBookService", iService, 20, 120, 180, 180, rootShape);

		IClass bookService = createClass(pkgService, "BookService", false);
		bookService.addStereotype("Subject");
		addAttr(bookService, "bookDAO", "IBookDAO", IAttribute.VISIBILITY_PRIVATE);
		addAttr(bookService, "listeners", "List<BookChangeListener>", IAttribute.VISIBILITY_PRIVATE);
		addOp(bookService, "BookService", "", param("bookDAO", "IBookDAO"));
		addOp(bookService, "getAllBooks", "List<Book>");
		addOp(bookService, "getBookByCode", "Book", param("bookCode", "String"));
		addOp(bookService, "addBook", "void", param("book", "Book"));
		addOp(bookService, "updateBook", "void", param("book", "Book"));
		addOp(bookService, "removeBook", "void", param("bookCode", "String"));
		addOp(bookService, "addBookChangeListener", "void", param("listener", "BookChangeListener"));
		addOp(bookService, "removeBookChangeListener", "void", param("listener", "BookChangeListener"));
		addOp(bookService, "validate", "void", param("book", "Book"));
		addOp(bookService, "notifyListeners", "void");
		placeClass(diagram, pkgService, "BookService", bookService, 220, 30, 180, 360, rootShape);

		realize(diagram, iService, bookService);

		// --- DAO ---
		IClass iDao = createClass(pkgDao, "IBookDAO", true);
		iDao.addStereotype("DAO");
		addOp(iDao, "findAll", "List<Book>");
		addOp(iDao, "findByCode", "Book", param("bookCode", "String"));
		addOp(iDao, "existsByCode", "boolean", param("bookCode", "String"));
		addOp(iDao, "insert", "void", param("book", "Book"));
		addOp(iDao, "update", "void", param("book", "Book"));
		addOp(iDao, "delete", "void", param("bookCode", "String"));
		placeClass(diagram, pkgDao, "IBookDAO", iDao, 20, 30, 180, 170, rootShape);

		IClass bookDao = createClass(pkgDao, "BookDAO", false);
		bookDao.addStereotype("DAO");
		addOp(bookDao, "findAll", "List<Book>");
		addOp(bookDao, "findByCode", "Book", param("bookCode", "String"));
		addOp(bookDao, "existsByCode", "boolean", param("bookCode", "String"));
		addOp(bookDao, "insert", "void", param("book", "Book"));
		addOp(bookDao, "update", "void", param("book", "Book"));
		addOp(bookDao, "delete", "void", param("bookCode", "String"));
		addOp(bookDao, "mapRow", "Book", param("rs", "ResultSet"));
		placeClass(diagram, pkgDao, "BookDAO", bookDao, 220, 30, 180, 230, rootShape);
		realize(diagram, iDao, bookDao);

		// --- Infrastructure Singleton ---
		IClass dbMgr = createClass(pkgUtil, "DBConnectionManager", false);
		dbMgr.addStereotype("Singleton");
		addAttr(dbMgr, "instance", "DBConnectionManager", IAttribute.VISIBILITY_PRIVATE);
		addAttr(dbMgr, "url", "String", IAttribute.VISIBILITY_PRIVATE);
		addAttr(dbMgr, "username", "String", IAttribute.VISIBILITY_PRIVATE);
		addAttr(dbMgr, "password", "String", IAttribute.VISIBILITY_PRIVATE);
		addOp(dbMgr, "DBConnectionManager", ""); // private ctor represented
		addOp(dbMgr, "getInstance", "DBConnectionManager").setVisibility(IOperation.VISIBILITY_PUBLIC);
		// mark getInstance as static via stereotype note
		addOp(dbMgr, "getConnection", "Connection");
		addOp(dbMgr, "closeQuietly", "void", param("closeable", "AutoCloseable"));
		placeClass(diagram, pkgUtil, "DBConnectionManager", dbMgr, 40, 40, 340, 180, rootShape);

		// --- Controller ---
		IClass controller = createClass(pkgController, "BookController", false);
		addAttr(controller, "view", "BookManagementFrame", IAttribute.VISIBILITY_PRIVATE);
		addAttr(controller, "bookService", "IBookService", IAttribute.VISIBILITY_PRIVATE);
		addAttr(controller, "books", "List<Book>", IAttribute.VISIBILITY_PRIVATE);
		addOp(controller, "BookController", "", param("view", "BookManagementFrame"), param("bookService", "IBookService"));
		addOp(controller, "initialize", "void");
		addOp(controller, "onNew", "void");
		addOp(controller, "onSave", "void");
		addOp(controller, "onRemove", "void");
		addOp(controller, "onExit", "void");
		addOp(controller, "onBookSelected", "void", param("index", "int"));
		placeClass(diagram, pkgController, "BookController", controller, 20, 30, 260, 220, rootShape);

		// --- Presentation ---
		IClass frame = createClass(pkgPresentation, "BookManagementFrame", false);
		frame.addStereotype("JFrame");
		addAttr(frame, "controller", "BookController", IAttribute.VISIBILITY_PRIVATE);
		addAttr(frame, "lstBooks", "JList<Book>", IAttribute.VISIBILITY_PRIVATE);
		addAttr(frame, "txtBookCode", "JTextField", IAttribute.VISIBILITY_PRIVATE);
		addAttr(frame, "txtBookName", "JTextField", IAttribute.VISIBILITY_PRIVATE);
		addAttr(frame, "txtAuthor", "JTextField", IAttribute.VISIBILITY_PRIVATE);
		addAttr(frame, "txtPublisher", "JTextField", IAttribute.VISIBILITY_PRIVATE);
		addAttr(frame, "cboPublishYear", "JComboBox<Integer>", IAttribute.VISIBILITY_PRIVATE);
		addAttr(frame, "chkForRent", "JCheckBox", IAttribute.VISIBILITY_PRIVATE);
		addOp(frame, "BookManagementFrame", "");
		addOp(frame, "onBooksChanged", "void");
		addOp(frame, "refreshBookList", "void", param("books", "List<Book>"));
		addOp(frame, "selectFirstBook", "void");
		addOp(frame, "clearForm", "void");
		addOp(frame, "fillForm", "void", param("book", "Book"));
		addOp(frame, "getFormBook", "Book");
		addOp(frame, "showError", "void", param("message", "String"));
		addOp(frame, "confirm", "boolean", param("message", "String"));
		addOp(frame, "setBookCodeEditable", "void", param("editable", "boolean"));
		placeClass(diagram, pkgPresentation, "BookManagementFrame", frame, 20, 30, 280, 230, rootShape);

		realize(diagram, listener, frame);

		// Associations / dependencies
		associate(diagram, frame, controller, "1", "1");
		associate(diagram, controller, iService, "1", "1");
		associate(diagram, bookService, iDao, "1", "1");
		associate(diagram, bookService, listener, "1", "0..*");
		dependency(diagram, bookDao, dbMgr);
		dependency(diagram, controller, book);
		dependency(diagram, bookService, book);
		dependency(diagram, bookDao, book);
		dependency(diagram, frame, book);

		// Notes
		addNote(diagram, 820, 580, 320, 160,
			"N-Layer (N=5):\n"
			+ "1 Presentation\n"
			+ "2 Controller\n"
			+ "3 Service (BLL)\n"
			+ "4 DAO (DAL)\n"
			+ "5 Infrastructure (util)\n"
			+ "Model shared. One-way deps.\n"
			+ "Patterns: DAO + Singleton + Observer");

		addNote(diagram, 820, 760, 320, 140,
			"SOLID:\n"
			+ "S - one reason to change\n"
			+ "O/D - IBookService / IBookDAO\n"
			+ "I - focused interfaces\n"
			+ "L - impls substitutable");

		// Keep headless-friendly: do not open diagram UI during CLI generation
	}

	// -------------------------------------------------------------------------
	// SEQUENCE: Add New Book
	// -------------------------------------------------------------------------
	private void createAddBookSequence() {
		IInteractionDiagramUIModel sequence = (IInteractionDiagramUIModel) dm.createDiagram(
				IDiagramTypeConstants.DIAGRAM_TYPE_INTERACTION_DIAGRAM);
		sequence.setName("Sequence - Add New Book");
		IFrame rootFrame = sequence.getRootFrame(true);

		String[] names = {
			"Librarian", "BookManagementFrame", "BookController", "BookService",
			"BookDAO", "DBConnectionManager", "SQL Server"
		};
		boolean[] isActor = { true, false, false, false, false, false, false };
		IInteractionLifeLine[] lines = new IInteractionLifeLine[names.length];
		IInteractionLifeLineUIModel[] shapes = new IInteractionLifeLineUIModel[names.length];
		IActivation[] acts = new IActivation[names.length];
		IActivationUIModel[] actShapes = new IActivationUIModel[names.length];

		int x = 60;
		for (int i = 0; i < names.length; i++) {
			lines[i] = factory.createInteractionLifeLine();
			rootFrame.addChild(lines[i]);
			lines[i].setName(names[i]);
			if (!isActor[i] && classes.containsKey(names[i])) {
				lines[i].setBaseClassifier(classes.get(names[i]));
			}
			shapes[i] = (IInteractionLifeLineUIModel) dm.createDiagramElement(sequence, lines[i]);
			shapes[i].setBounds(x, 30, 110, 720);
			shapes[i].resetCaption();

			acts[i] = factory.createActivation();
			lines[i].addActivation(acts[i]);
			actShapes[i] = (IActivationUIModel) dm.createDiagramElement(sequence, acts[i]);
			actShapes[i].setBounds(x + 46, 100, IActivationUIModel.BODY_WIDTH, 560);
			x += 160;
		}

		int y = 120;
		int step = 45;
		// 1 Librarian -> Frame : fill form + click Save
		msg(sequence, "1: fill form + click Save", acts[0], acts[1], shapes[0], shapes[1], y); y += step;
		// 2 Frame -> Controller : onSave()
		msg(sequence, "2: onSave()", acts[1], acts[2], shapes[1], shapes[2], y); y += step;
		// 3 Controller -> Controller : getFormBook()
		msg(sequence, "3: getFormBook()", acts[2], acts[2], shapes[2], shapes[2], y); y += step;
		// 4 Controller -> Service : addBook(book)
		msg(sequence, "4: addBook(book)", acts[2], acts[3], shapes[2], shapes[3], y); y += step;
		// 5 Service -> Service : validate(book)
		msg(sequence, "5: validate(book)", acts[3], acts[3], shapes[3], shapes[3], y); y += step;
		// 6 Service -> DAO : existsByCode(code)
		msg(sequence, "6: existsByCode(code)", acts[3], acts[4], shapes[3], shapes[4], y); y += step;
		// 7 DAO -> Singleton : getInstance().getConnection()
		msg(sequence, "7: getInstance().getConnection()", acts[4], acts[5], shapes[4], shapes[5], y); y += step;
		// 8 Singleton -> DB
		msg(sequence, "8: open connection", acts[5], acts[6], shapes[5], shapes[6], y); y += step;
		// 9 return connection
		msg(sequence, "9: Connection", acts[6], acts[5], shapes[6], shapes[5], y); y += step;
		msg(sequence, "10: Connection", acts[5], acts[4], shapes[5], shapes[4], y); y += step;
		// 11 DAO insert
		msg(sequence, "11: INSERT PreparedStatement", acts[4], acts[6], shapes[4], shapes[6], y); y += step;
		msg(sequence, "12: OK", acts[6], acts[4], shapes[6], shapes[4], y); y += step;
		msg(sequence, "13: return", acts[4], acts[3], shapes[4], shapes[3], y); y += step;
		// Observer notify
		msg(sequence, "14: notifyListeners()", acts[3], acts[3], shapes[3], shapes[3], y); y += step;
		msg(sequence, "15: onBooksChanged() <<Observer>>", acts[3], acts[1], shapes[3], shapes[1], y); y += step;
		msg(sequence, "16: getAllBooks()", acts[1], acts[2], shapes[1], shapes[2], y); y += step;
		msg(sequence, "17: getAllBooks()", acts[2], acts[3], shapes[2], shapes[3], y); y += step;
		msg(sequence, "18: findAll()", acts[3], acts[4], shapes[3], shapes[4], y); y += step;
		msg(sequence, "19: List<Book>", acts[4], acts[3], shapes[4], shapes[3], y); y += step;
		msg(sequence, "20: List<Book>", acts[3], acts[2], shapes[3], shapes[2], y); y += step;
		msg(sequence, "21: refreshBookList(books)", acts[2], acts[1], shapes[2], shapes[1], y); y += step;
		msg(sequence, "22: show new item in JList", acts[1], acts[0], shapes[1], shapes[0], y);
	}

	// -------------------------------------------------------------------------
	// SEQUENCE: Remove Book
	// -------------------------------------------------------------------------
	private void createRemoveBookSequence() {
		IInteractionDiagramUIModel sequence = (IInteractionDiagramUIModel) dm.createDiagram(
				IDiagramTypeConstants.DIAGRAM_TYPE_INTERACTION_DIAGRAM);
		sequence.setName("Sequence - Remove Book");
		IFrame rootFrame = sequence.getRootFrame(true);

		String[] names = {
			"Librarian", "BookManagementFrame", "BookController", "BookService",
			"BookDAO", "DBConnectionManager", "SQL Server"
		};
		IInteractionLifeLine[] lines = new IInteractionLifeLine[names.length];
		IInteractionLifeLineUIModel[] shapes = new IInteractionLifeLineUIModel[names.length];
		IActivation[] acts = new IActivation[names.length];

		int x = 60;
		for (int i = 0; i < names.length; i++) {
			lines[i] = factory.createInteractionLifeLine();
			rootFrame.addChild(lines[i]);
			lines[i].setName(names[i]);
			if (classes.containsKey(names[i])) {
				lines[i].setBaseClassifier(classes.get(names[i]));
			}
			shapes[i] = (IInteractionLifeLineUIModel) dm.createDiagramElement(sequence, lines[i]);
			shapes[i].setBounds(x, 30, 110, 780);
			shapes[i].resetCaption();

			acts[i] = factory.createActivation();
			lines[i].addActivation(acts[i]);
			IActivationUIModel actShape = (IActivationUIModel) dm.createDiagramElement(sequence, acts[i]);
			actShape.setBounds(x + 46, 100, IActivationUIModel.BODY_WIDTH, 620);
			x += 160;
		}

		int y = 120;
		int step = 42;
		msg(sequence, "1: select book + click Remove", acts[0], acts[1], shapes[0], shapes[1], y); y += step;
		msg(sequence, "2: onRemove()", acts[1], acts[2], shapes[1], shapes[2], y); y += step;
		msg(sequence, "3: confirm(\"Are you sure?\")", acts[2], acts[1], shapes[2], shapes[1], y); y += step;
		msg(sequence, "4: show confirm dialog", acts[1], acts[0], shapes[1], shapes[0], y); y += step;
		msg(sequence, "5: [Confirm] Yes", acts[0], acts[1], shapes[0], shapes[1], y); y += step;
		msg(sequence, "6: true", acts[1], acts[2], shapes[1], shapes[2], y); y += step;
		msg(sequence, "7: removeBook(bookCode)", acts[2], acts[3], shapes[2], shapes[3], y); y += step;
		msg(sequence, "8: delete(bookCode)", acts[3], acts[4], shapes[3], shapes[4], y); y += step;
		msg(sequence, "9: getInstance().getConnection()", acts[4], acts[5], shapes[4], shapes[5], y); y += step;
		msg(sequence, "10: Connection", acts[5], acts[4], shapes[5], shapes[4], y); y += step;
		msg(sequence, "11: DELETE PreparedStatement", acts[4], acts[6], shapes[4], shapes[6], y); y += step;
		msg(sequence, "12: OK", acts[6], acts[4], shapes[6], shapes[4], y); y += step;
		msg(sequence, "13: return", acts[4], acts[3], shapes[4], shapes[3], y); y += step;
		msg(sequence, "14: notifyListeners()", acts[3], acts[3], shapes[3], shapes[3], y); y += step;
		msg(sequence, "15: onBooksChanged() <<Observer>>", acts[3], acts[1], shapes[3], shapes[1], y); y += step;
		msg(sequence, "16: getAllBooks()", acts[1], acts[2], shapes[1], shapes[2], y); y += step;
		msg(sequence, "17: getAllBooks()/findAll()", acts[2], acts[3], shapes[2], shapes[3], y); y += step;
		msg(sequence, "18: List<Book>", acts[3], acts[2], shapes[3], shapes[2], y); y += step;
		msg(sequence, "19: refreshBookList(books)", acts[2], acts[1], shapes[2], shapes[1], y); y += step;
		msg(sequence, "20: alt [list not empty]", acts[1], acts[1], shapes[1], shapes[1], y); y += step;
		msg(sequence, "21: selectFirstBook() + fillForm(first)", acts[1], acts[1], shapes[1], shapes[1], y); y += step;
		msg(sequence, "22: [else] clearForm()", acts[1], acts[1], shapes[1], shapes[1], y); y += step;
		msg(sequence, "23: display first remaining book", acts[1], acts[0], shapes[1], shapes[0], y);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------
	private IPackage addPackage(IPackage parent, String name, int x, int y, int w, int h,
			IClassDiagramUIModel diagram, IPackageUIModel rootShape) {
		IPackage pkg = factory.createPackage();
		pkg.setName(name);
		parent.addChild(pkg);
		IPackageUIModel shape = (IPackageUIModel) dm.createDiagramElement(diagram, pkg);
		shape.setBounds(x, y, w, h);
		rootShape.addChild(shape);
		shape.setRequestResetCaption(true);
		packageShapes.put(name, shape);
		return pkg;
	}

	private IClass createClass(IPackage parent, String name, boolean isInterface) {
		IClass cls = factory.createClass();
		cls.setName(name);
		cls.setVisibility(IClass.VISIBILITY_PUBLIC);
		if (isInterface) {
			cls.addStereotype("Interface");
		}
		parent.addChild(cls);
		classes.put(name, cls);
		return cls;
	}

	private void placeClass(IClassDiagramUIModel diagram, IPackage pkg, String key, IClass cls,
			int x, int y, int w, int h, IPackageUIModel rootShape) {
		IClassUIModel shape = (IClassUIModel) dm.createDiagramElement(diagram, cls);
		shape.setBounds(x, y, w, h);
		IPackageUIModel pkgShape = packageShapes.get(pkg.getName());
		if (pkgShape != null) {
			pkgShape.addChild(shape);
		} else {
			rootShape.addChild(shape);
		}
		shape.setRequestResetCaption(true);
		classShapes.put(key, shape);
	}

	private void addAttr(IClass cls, String name, String type, String visibility) {
		IAttribute attr = factory.createAttribute();
		attr.setName(name);
		attr.setType(type);
		attr.setVisibility(visibility);
		cls.addAttribute(attr);
	}

	private IParameter param(String name, String type) {
		IParameter p = factory.createParameter();
		p.setName(name);
		p.setType(type);
		return p;
	}

	private IOperation addOp(IClass cls, String name, String returnType, IParameter... params) {
		IOperation op = factory.createOperation();
		op.setName(name);
		op.setVisibility(IOperation.VISIBILITY_PUBLIC);
		if (returnType != null && returnType.length() > 0) {
			op.setReturnType(returnType);
		}
		if (params != null) {
			for (IParameter p : params) {
				op.addParameter(p);
			}
		}
		cls.addOperation(op);
		return op;
	}

	private void realize(IClassDiagramUIModel diagram, IClass fromInterface, IClass toImpl) {
		IRealization r = factory.createRealization();
		r.setFrom(fromInterface);
		r.setTo(toImpl);
		dm.createConnector(diagram, r, classShapes.get(fromInterface.getName()), classShapes.get(toImpl.getName()), null);
	}

	private void generalize(IClassDiagramUIModel diagram, IClass parent, IClass child) {
		IGeneralization g = factory.createGeneralization();
		g.setFrom(parent);
		g.setTo(child);
		dm.createConnector(diagram, g, classShapes.get(parent.getName()), classShapes.get(child.getName()), null);
	}

	private void associate(IClassDiagramUIModel diagram, IClass from, IClass to, String fromMul, String toMul) {
		IAssociation a = factory.createAssociation();
		a.setFrom(from);
		a.setTo(to);
		((IAssociationEnd) a.getFromEnd()).setMultiplicity(fromMul);
		((IAssociationEnd) a.getToEnd()).setMultiplicity(toMul);
		dm.createConnector(diagram, a, classShapes.get(from.getName()), classShapes.get(to.getName()), null);
	}

	private void dependency(IClassDiagramUIModel diagram, IClass from, IClass to) {
		IDependency d = factory.createDependency();
		d.setFrom(from);
		d.setTo(to);
		dm.createConnector(diagram, d, classShapes.get(from.getName()), classShapes.get(to.getName()), null);
	}

	private void addNote(IClassDiagramUIModel diagram, int x, int y, int w, int h, String text) {
		INOTE note = factory.createNOTE();
		note.setName("Note");
		note.setDocumentation(text);
		INoteUIModel shape = (INoteUIModel) dm.createDiagramElement(diagram, note);
		shape.setBounds(x, y, w, h);
		shape.setRequestResetCaption(true);
	}

	private void msg(IInteractionDiagramUIModel sequence, String name,
			IActivation fromAct, IActivation toAct,
			IInteractionLifeLineUIModel fromShape, IInteractionLifeLineUIModel toShape, int y) {
		IMessage message = factory.createMessage();
		message.setName(name);
		message.setFromActivation(fromAct);
		message.setToActivation(toAct);
		int fromX = fromShape.getX() + fromShape.getWidth() / 2;
		int toX = toShape.getX() + toShape.getWidth() / 2;
		IMessageUIModel shape = (IMessageUIModel) dm.createConnector(sequence, message, fromShape, toShape,
				new Point[] { new Point(fromX, y), new Point(toX, y) });
		shape.resetCaption();
	}
}
