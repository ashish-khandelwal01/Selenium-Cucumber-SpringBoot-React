import { useTags } from '../hooks/useTags';

const TagDropdown = () => {
  const { tags, loading, error } = useTags();

  if (loading) return <p>Loading tags...</p>;
  if (error) return <p className="text-red-600">{error}</p>;

  return (
    <select className="p-2 border rounded">
      <option value="">Select a tag</option>
      {tags.map((tag) => (
        <option key={tag.name}>{tag.name}</option>
    ))}
    </select>
  );
};

export default TagDropdown;
