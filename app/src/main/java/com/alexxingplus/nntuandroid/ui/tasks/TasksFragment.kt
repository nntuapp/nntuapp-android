package com.alexxingplus.nntuandroid.ui.tasks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alexxingplus.nntuandroid.R
import com.alexxingplus.nntuandroid.databinding.FragmentTasksBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.concurrent.thread

class TasksFragment: Fragment() {
    private var updateClosure: (Task) -> Unit = {}

    override fun onResume() {
        super.onResume()
        intentDeleteTask = null
        intentDoneTask = null
        val task = intentTask ?: return
        updateClosure(task)
        intentTask = null
    }

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        val view = binding.root

        // updateLastID(activity as MainActivity?, requireContext())

        var data = ArrayList<Task>()
        val provider = TaskProvider(requireContext())
        val taskStorage = TaskStorage(requireContext(), null)
        /*val list: ListView = root.findViewById(R.id.taskList)
        val newTaskButton: FloatingActionButton = root.findViewById(R.id.newTaskButton)
        val ptr: SwipeRefreshLayout = root.findViewById(R.id.taskPtr)
        var nothingFoundStack: LinearLayout = root.findViewById(R.id.nothingFoundStack)*/
        val userDefaults = activity?.getPreferences(Context.MODE_PRIVATE) ?: return view
        var group = userDefaults.getString("group", "")
        group = group?.replace("-", "")?.replace(" ", "")?.replace("_","")
        if (group == null || group.isEmpty()) {
            binding.newTaskButton.isVisible = false
            binding.newTaskButton.isEnabled = false
        }

        fun nothingFound(shouldShow: Boolean) {
            binding.noTasksLinearLayout.isVisible = shouldShow
        }

        fun reloadData(){
            requireActivity().runOnUiThread {
                nothingFound(data.isEmpty())
                val adapter = binding.taskListView.adapter as TaskAdapter
                adapter.tasks = ArrayList(data.sortedBy { it.deadline })
                adapter.notifyDataSetChanged()
            }
        }

        val deleteClosure: (Task) -> Unit = { task ->
            provider.delete(task.id){ done ->
                if (done) {
                    data.remove(task)
                    reloadData()
                }
            }
        }

        binding.taskListView.adapter = TaskAdapter(data, requireContext(), requireActivity(), taskStorage, deleteClosure)

        thread {
            val localTasks = taskStorage.load()
            if (data.isEmpty()) {
                data = localTasks
                reloadData()
            }
        }

        fun loadTasks(){
            binding.tasksSwipeRefreshLayout.isRefreshing = true
            val groupName = group ?: return
            provider.download(groupName) { tasks ->
                if (tasks == null) {
                    binding.tasksSwipeRefreshLayout.isRefreshing = false
                    return@download
                }
                data = combine(local = data, net = tasks)
                taskStorage.save(data)
                reloadData()
                binding.tasksSwipeRefreshLayout.isRefreshing = false
            }
        }

        updateClosure = { task ->
            data.add(task)
            taskStorage.save(data)
            reloadData()
            loadTasks()
        }

        loadTasks()
        binding.tasksSwipeRefreshLayout.setOnRefreshListener {
            loadTasks()
        }

        binding.newTaskButton.setOnClickListener {
            val groupName = group ?: return@setOnClickListener
            val intent = Intent(requireContext(), TaskEditor::class.java)
            intent.putExtra("group", groupName)
            requireContext().startActivity(intent)
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class TaskAdapter(
        data: ArrayList<Task>,
        context: Context,
        activity: FragmentActivity,
        storage: TaskStorage,
        val deleteClosure: (Task) -> Unit
    ): BaseAdapter(){
        var tasks = data
        private var mContext = context
        private val taskStorage = storage
        private val fragmentActivity = activity

        override fun getCount(): Int {
            return tasks.size
        }

        override fun getItem(p0: Int): Any {
            return "Test String"
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(mContext)
            val task = tasks[position]
            val cell: View = inflater.inflate(R.layout.task_cell, parent, false)
            val card: CardView = cell.findViewById(R.id.taskCellCard)
            val title: TextView = cell.findViewById(R.id.taskCellTitle)
            val subject: TextView = cell.findViewById(R.id.taskCellSubject)
            val deadline: TextView = cell.findViewById(R.id.taskCellDeadline)
            val checkBox: CheckBox = cell.findViewById(R.id.taskCellCheckBox)
            title.text = task.title
            subject.text = task.subject
            deadline.text = task.deadline.description
            checkBox.isChecked = task.done
            checkBox.setOnClickListener {
                task.done = checkBox.isChecked
                taskStorage.save(tasks)
            }
            card.setOnClickListener {
                intentDoneTask = { done ->
                    task.done = done
                    checkBox.isChecked = done
                    taskStorage.save(tasks)
                }
                intentDeleteTask = { id ->
                    deleteClosure(task)
                }
                val intent = Intent(mContext, SingleTask::class.java)
                intent.putExtra("task", task)
                mContext.startActivity(intent)
            }
            return cell
        }
    }

    private fun combine(local: ArrayList<Task>, net: ArrayList<Task>): ArrayList<Task> {
        val localMap = local.hashMap
        val netMap = net.hashMap
        for (key in netMap.keys){
            if (localMap[key] != null) {
                netMap[key]!!.done = localMap[key]!!.done
            }
        }
        return ArrayList(netMap.values)
    }

    private val ArrayList<Task>.hashMap: HashMap<Int, Task>
    get() {
        val map = HashMap<Int, Task>()
        for (task in this){
            map[task.id] = task
        }
        return map
    }
}