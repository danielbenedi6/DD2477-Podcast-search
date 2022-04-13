import os
import argparse
import glob
import csv

def read_compliance_list(path):
    """
    Reading a list of episode uris that is needed to be modified.

    """
    with open(path,'r') as f:
        data = f.readlines()
    return [x.strip().split(',') for x in data]

def read_metadata(path):
    """Reading metadata"""
    with open(path,'r') as f:
        file = csv.reader(f,delimiter='\t')
        data = [x for x in file]
    return data
def write_metadata(path,data):
    """Writing metadata"""
    with open(path,'w') as f:
        filewriter = csv.writer(f, delimiter='\t')
        for row in data:
            filewriter.writerow(row)

def main(args):
    data_dir  = args.data_dir
    path = args.path
    episodes = read_compliance_list(path)
    if not episodes:
        print("Nothing to be deleted.")
        return

    if not os.path.exists(os.path.join(data_dir,'metadata.tsv')):
        print("Missing metadata ./metadata.tsv.")
        return


    print(f"{len(episodes)} episode(s) are waiting to be deleted.")

    # read metadata
    print("Reading metadata...")
    metadata = read_metadata(os.path.join(data_dir,'metadata.tsv'))
    print(f"{len(metadata)-1} episodes before the deletion.")

    print("Starting to delete the transcripts and audio files...")
    for show, episode in episodes:

        #TODO 1 delete the transcripts file
        transcripts_file_path  = glob.glob(os.path.join(data_dir,'podcasts-transcripts','*','*',show,episode+'.json'))
        if transcripts_file_path:
            os.remove(transcripts_file_path[0])
            print("Removing transcript:",episode+'.json')
        else:
            print(f"Transcript {episode}.json does not exist.")

        #TODO 2 delete the audio file
        audio_file_path = glob.glob(os.path.join(data_dir,'podcasts-audio','*','*',show,episode+'.ogg'))
        if audio_file_path:
            os.remove(audio_file_path[0])
            print("Removing audio file:",episode+'.ogg')
        else:
            print(f"Audio file {episode}.ogg does not exist.")

    print("Updating metadata.")

    #TODO 3 update metadata
    metadata_new = []
    show_del = []
    shows_old = {x[10] for x in metadata}
    episodes_dict = {x[1] for x in episodes}
    for idx,row in enumerate(metadata):
        if row[11] not in episodes_dict:
            metadata_new.append(row)
    print(f"{len(metadata_new)-1} episodes after the deletion.")
    shows_new = {x[10] for x in metadata_new}
    # generate difference of shows for the deletion of the rss header
    show_del = list(shows_old - shows_new)
    # writing the resulting file to the original file
    print("Saving new metadata....")
    write_metadata(os.path.join(data_dir,'metadata.tsv'),metadata_new)

    #TODO 4 delete rss xml file as needed
    if show_del:
        print("Updating rss file.")
    for show in show_del:
        rss_path = glob.glob(os.path.join(data_dir,'show-rss','*','*',show+'.xml'))
        if rss_path:
            os.remove(rss_path[0])
            print("Removing rss header:", show+'.xml')
        else:
            print(f"RSS file {show}.xml does not exist.")

    #TODO 5 delete empty show folder for podcasts-transcripts and  podcasts-audio
    for show in show_del:
        transcripts_show_path = glob.glob(os.path.join(data_dir,'podcasts-transcripts',show[5],show[6].upper(),show))
        if transcripts_show_path:
            os.rmdir(transcripts_show_path[0])
        audio_show_path = glob.glob(os.path.join(data_dir,'podcasts-audio',show[5],show[6].upper(),show))
        if audio_show_path:
            os.rmdir(audio_show_path[0])
    print("Done.")

if __name__=="__main__":
    """
    Usage :
    
    $ cd spotify-podcasts-2020/
    $ python3 scripts/compliance.py
     (or python3 scripts/compliance.py -p scripts/delete_file.txt -d ./)
    
    Notes:
    In data directory, it should have the files structure as following:
    
    $ spotify-podcasts-2020/podcasts-transcripts/0/A/show_*/*.json
    $ spotify-podcasts-2020/podcasts-audio/0/A/show_*/*.ogg
    $ spotify-podcasts-2020/show-rss/0/A/show_*.xml
    $ spotify-podcasts-2020/metadata.tsv 
    $ spotify-podcasts-2020/scripts/compliance.py
    $ spotify-podcasts-2020/scripts/delete_file.txt
    
    Example "delete_file.txt":
    The "delete_file.txt" has two columns which are show_filename_prefix and episode_filename_prefix.
    It could be empty when nothing needs to be deleted.
    Example : 
        show_0F2zZNU9wzNSfAW1IJTjU2,2rPk0aN8NIArjJuJEqz8KL
        show_0F2zZNU9wzNSfAW1IJTjU2,5m0lPlDNjMeFjtukFBFpiC
        show_0f2P0fH4EwuEtXKpXIt7Ui,0BDVyuIPWhu8XoG5y9m7nF
    
    """
    parser = argparse.ArgumentParser("Delete a list of episodes from dataset")
    parser.add_argument("-p", "--path", type=str, help="path where deletion list is located", default="scripts/delete_file.txt")
    parser.add_argument("-d", "--data_dir", type=str, help="data dir where all data sit", default="./")
    args = parser.parse_args()
    main(args)
