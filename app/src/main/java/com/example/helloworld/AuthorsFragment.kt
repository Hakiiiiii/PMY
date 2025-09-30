import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.helloworld.R

class AuthorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_authors, container, false)

        val lvAuthors = view.findViewById<ListView>(R.id.lvAuthors)

        val authors = listOf(
            Author("Студент1", R.drawable.author1),
            Author("Студент2", R.drawable.author2)
        )

        val adapter = AuthorAdapter(requireContext(), authors)
        lvAuthors.adapter = adapter

        return view
    }
}

data class Author(val name: String, val photoRes: Int)

class AuthorAdapter(context: Context, authors: List<Author>) : ArrayAdapter<Author>(context, 0, authors) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_author, parent, false)
        val author = getItem(position)
        view.findViewById<ImageView>(R.id.ivPhoto).setImageResource(author!!.photoRes)
        view.findViewById<TextView>(R.id.tvName).text = author.name
        return view
    }
}